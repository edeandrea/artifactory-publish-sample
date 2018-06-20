package com.example.build.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class JavaPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.apply plugin: 'java'

		configureArtifactory project
		configureMaven project
	}

	private void configureArtifactory(Project project) {
		project.apply plugin: 'com.jfrog.artifactory'
		project.apply plugin: 'maven-publish'

		project.configure(project) {
			repositories {
				maven {
					url 'https://repo.spring.io/libs-release'
				}
			}

			artifactory {
				contextUrl = 'https://repo.spring.io'

				publish {
					repository {
						repoKey = 'libs-snapshot'
					}
				}

				clientConfig.includeEnvVars = true
				clientConfig.envVarsExcludePatterns = '*pwd*,*password*,*PWD*,*PASSWORD*,*Password,*secret*,*SECRET*,*key*,*KEY*,sonar.login,GRADLE_OPTS'
				clientConfig.info.buildName = 'artifactory-publish-sample'
				clientConfig.info.buildNumber = version
			}
		}
	}

	private void configureMaven(Project project) {
		project.afterEvaluate { proj ->
			// Artifactory has already been configured on the root project by the com.aig.base plugin
			// Just need to add the main publication
			def bootJarTask = proj.tasks.findByName 'bootJar'
			def jarTask = bootJarTask?.enabled ? bootJarTask : proj.tasks['jar']

			proj.configure(proj.publishing) {
				publications {
					mainJava(MavenPublication) {
						artifactId 'artifactory-publish-sample'
						// Change due to https://github.com/gradle/gradle/issues/1061
						//						from proj.components.java
						artifact jarTask
					}
				}
			}

			// And then configure the artifactoryPublish task to publish the main publication
			proj.configure(proj.tasks['artifactoryPublish']) {
				publications 'mainJava'
			}
		}
	}
}
