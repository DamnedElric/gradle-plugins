package io.freefair.gradle.plugins.github.internal;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;


class GitUtilsTest {

    private Project project;

    @TempDir
    private File gradleUserHome;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(new File("."))
                .withGradleUserHomeDir(gradleUserHome)
                .build();

    }

    @Test
    void findSlug() {
        assertThat(GitUtils.findSlug(project))
                .isEqualTo("freefair/gradle-plugins");
    }

    @Test
    void getRemoteUrl() {
        assertThat(GitUtils.getRemoteUrl(project, "origin"))
                .contains("freefair/gradle-plugins");
    }

    @Test
    void findWorkingDirectory() {
        assertThat(GitUtils.findWorkingDirectory(project)).isNotNull();
    }

    @Test
    void getTag() {
        assertThat(GitUtils.getTag(project)).isNotNull();
    }
}
