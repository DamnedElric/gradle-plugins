package io.freefair.gradle.plugins.maven.war;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;

import java.io.File;
import java.util.Collections;

public class WarArchiveClassesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getTasks().withType(War.class, war -> {

            Property<Boolean> archiveClasses = project.getObjects().property(Boolean.class).convention(false);
            war.getExtensions().add("archiveClasses", archiveClasses);

            TaskProvider<Jar> warClassesJar = project.getTasks().register(war.getName() + "ClassesJar", Jar.class, jar -> {
                jar.getArchiveBaseName().convention(war.getArchiveBaseName());
                jar.getArchiveAppendix().convention(war.getArchiveAppendix());
                jar.getArchiveVersion().convention(war.getArchiveVersion());
                jar.getArchiveClassifier().convention(war.getArchiveClassifier());
                jar.getDestinationDirectory().set(war.getTemporaryDir());
            });

            project.afterEvaluate(p -> {

                warClassesJar.configure(jar -> jar.setEnabled(archiveClasses.get()));

                if (archiveClasses.get()) {
                    FileCollection warClasspath = war.getClasspath();

                    warClassesJar.configure(jar -> jar.from(warClasspath != null ? warClasspath.filter(File::isDirectory) : Collections.emptyList()));

                    war.setClasspath(warClasspath == null ? null : warClasspath.filter(File::isFile).plus(project.files(warClassesJar)));
                }
            });
        });
    }
}
