package io.freefair.gradle.plugins.sass;

import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.War;

import java.io.File;

@Incubating
public class SassWarPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(SassWebjarsPlugin.class);
        project.getPlugins().apply(WarPlugin.class);

        TaskProvider<SassCompile> sassCompileTaskProvider = project.getTasks().register("compileWebappSass", SassCompile.class, compileWebappSass -> {
            compileWebappSass.setGroup(BasePlugin.BUILD_GROUP);
            compileWebappSass.setDescription("Compile sass and scss files for the webapp");

            WarPluginConvention warPluginConvention = project.getConvention().getPlugin(WarPluginConvention.class);
            compileWebappSass.source(warPluginConvention.getWebAppDir());

            compileWebappSass.getDestinationDir().set(new File(project.getBuildDir(), "jsass/webapp"));
        });

        project.getTasks().named(WarPlugin.WAR_TASK_NAME, War.class)
                .configure(war -> war.from(sassCompileTaskProvider));
    }
}
