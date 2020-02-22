package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import java.io.File;

@Getter
@Deprecated
public class JavadocJarPlugin implements Plugin<Project> {

    private TaskProvider<Jar> javadocJar;
    private TaskProvider<Jar> aggregateJavadocJar;

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {

            project.getLogger().warn("io.freefair.javadoc-jar is deprecated. Use java.withJavadocJar() instead");

            javadocJar = project.getTasks().register("javadocJar", Jar.class, javadocJar -> {
                javadocJar.from(project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME));
                javadocJar.getArchiveClassifier().set("javadoc");
                javadocJar.setDescription("Assembles a jar archive containing the javadocs.");
                javadocJar.setGroup(BasePlugin.BUILD_GROUP);
            });

            project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, javadocJar);
        });

        project.getPlugins().withType(AggregateJavadocPlugin.class, aggregateJavadocPlugin -> {
            aggregateJavadocJar = project.getTasks().register("aggregateJavadocJar", Jar.class, aggregateJavadocJar -> {
                aggregateJavadocJar.from(aggregateJavadocPlugin.getAggregateJavadoc());
                aggregateJavadocJar.getArchiveClassifier().set("javadoc");
                aggregateJavadocJar.setGroup(BasePlugin.BUILD_GROUP);
            });

            project.getPlugins().apply(BasePlugin.class);
            project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, aggregateJavadocJar);

            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                aggregateJavadocJar.configure(aggregateJavadocJar -> {

                    aggregateJavadocJar.getArchiveClassifier().convention("aggregateJavadoc");
                    aggregateJavadocJar.getDestinationDirectory().set(new File(
                            project.getConvention().getPlugin(JavaPluginConvention.class).getDocsDir(),
                            "aggregateJavadoc"
                    ));
                });
            });

        });
    }
}
