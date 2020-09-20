package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.DefaultWeavingSourceSet;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.scala.ScalaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.scala.ScalaCompile;

@NonNullApi
public class AspectJPostCompileWeavingPlugin implements Plugin<Project> {

    private Project project;
    private AspectJBasePlugin aspectjBasePlugin;

    @Override
    public void apply(Project project) {
        if (project.getPlugins().hasPlugin(AspectJPlugin.class)) {
            throw new IllegalStateException("Another aspectj plugin (which is excludes this one) has already been applied to the project.");
        }

        this.project = project;
        aspectjBasePlugin = project.getPlugins().apply(AspectJBasePlugin.class);

        project.getPlugins().apply(JavaBasePlugin.class);

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(this::configureSourceSet);
    }

    private void configureSourceSet(SourceSet sourceSet) {
        project.afterEvaluate(p ->
                p.getDependencies().add(sourceSet.getImplementationConfigurationName(), "org.aspectj:aspectjrt:" + aspectjBasePlugin.getAspectjExtension().getVersion().get())
        );

        DefaultWeavingSourceSet weavingSourceSet = new DefaultWeavingSourceSet(sourceSet);
        new DslObject(sourceSet).getConvention().add("aspectj", weavingSourceSet);

        Configuration aspectpath = project.getConfigurations().create(weavingSourceSet.getAspectConfigurationName());
        weavingSourceSet.setAspectPath(aspectpath);

        Configuration inpath = project.getConfigurations().create(weavingSourceSet.getInpathConfigurationName());
        weavingSourceSet.setInPath(inpath);

        project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName())
                .extendsFrom(aspectpath);

        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin ->
                project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, compileJava -> {
                    AjcAction ajcAction = enhanceWithWeavingAction(compileJava, aspectpath, inpath, aspectjBasePlugin.getAspectjConfiguration());
                    ajcAction.getOptions().getBootclasspath().from(compileJava.getOptions().getBootstrapClasspath());
                    ajcAction.getOptions().getExtdirs().from(compileJava.getOptions().getExtensionDirs());
                })
        );

        project.getPlugins().withType(GroovyPlugin.class, groovyPlugin ->
                project.getTasks().named(sourceSet.getCompileTaskName("groovy"), GroovyCompile.class, compileGroovy -> {
                    AjcAction ajcAction = enhanceWithWeavingAction(compileGroovy, aspectpath, inpath, aspectjBasePlugin.getAspectjConfiguration());
                    ajcAction.getOptions().getBootclasspath().from(compileGroovy.getOptions().getBootstrapClasspath());
                    ajcAction.getOptions().getExtdirs().from(compileGroovy.getOptions().getExtensionDirs());
                })
        );

        project.getPlugins().withType(ScalaBasePlugin.class, scalaBasePlugin ->
                project.getTasks().named(sourceSet.getCompileTaskName("scala"), ScalaCompile.class, compileScala -> {
                    AjcAction ajcAction = enhanceWithWeavingAction(compileScala, aspectpath, inpath, aspectjBasePlugin.getAspectjConfiguration());
                    ajcAction.getOptions().getBootclasspath().from(compileScala.getOptions().getBootstrapClasspath());
                    ajcAction.getOptions().getExtdirs().from(compileScala.getOptions().getExtensionDirs());
                })
        );

        project.getPlugins().withId("org.jetbrains.kotlin.jvm", kotlinPlugin ->
                project.getTasks().named(sourceSet.getCompileTaskName("kotlin"), AbstractCompile.class, compileKotlin ->
                        enhanceWithWeavingAction(compileKotlin, aspectpath, inpath, aspectjBasePlugin.getAspectjConfiguration())
                )
        );
    }

    private AjcAction enhanceWithWeavingAction(AbstractCompile abstractCompile, Configuration aspectpath, Configuration inpath, Configuration aspectjConfiguration) {
        AjcAction action = project.getObjects().newInstance(AjcAction.class);

        action.getOptions().getAspectpath().from(aspectpath);
        action.getOptions().getInpath().from(inpath);
        action.getAdditionalInpath().from(abstractCompile.getDestinationDir());
        action.getClasspath().from(aspectjConfiguration);

        action.addToTask(abstractCompile);

        return action;
    }

}
