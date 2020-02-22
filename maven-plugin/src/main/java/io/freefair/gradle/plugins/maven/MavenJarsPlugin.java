package io.freefair.gradle.plugins.maven;

import io.freefair.gradle.plugins.maven.javadoc.JavadocJarPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
@Deprecated
public class MavenJarsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getLogger().warn("io.freefair.maven-jars is deprecated. Use java.withSourcesJar() and java.withJavadocJar() instead");

        project.getPluginManager().apply(SourcesJarPlugin.class);
        project.getPluginManager().apply(JavadocJarPlugin.class);
    }
}
