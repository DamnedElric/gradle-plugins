package io.freefair.gradle.plugins.maven.war;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
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

            WarArchiveClassesConvention archiveClassesConvention = new WarArchiveClassesConvention(archiveClasses);
            war.getConvention().getPlugins().put("archiveClasses", archiveClassesConvention);

            Jar warClassesJar = project.getTasks().create(war.getName() + "ClassesJar", Jar.class);
            warClassesJar.getArchiveBaseName().convention(war.getArchiveBaseName());
            warClassesJar.getArchiveAppendix().convention(war.getArchiveAppendix());
            warClassesJar.getArchiveVersion().convention(war.getArchiveVersion());
            warClassesJar.getArchiveClassifier().convention(war.getArchiveClassifier());

            project.afterEvaluate(p -> {

                warClassesJar.setEnabled(archiveClasses.get());

                if (archiveClasses.get()) {
                    FileCollection warClasspath = war.getClasspath();

                    warClassesJar.from(warClasspath != null ? warClasspath.filter(File::isDirectory) : Collections.emptyList());

                    war.setClasspath(warClasspath == null ? null : warClasspath.filter(File::isFile).plus(project.files(warClassesJar)));
                }
            });
        });
    }
}
