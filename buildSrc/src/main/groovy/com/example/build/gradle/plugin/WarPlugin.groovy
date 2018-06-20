package com.example.build.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class WarPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.apply plugin: JavaPlugin
		project.apply plugin: 'war'
		project.apply plugin: 'eclipse-wtp'

		configurePublications project
	}

	private void configurePublications(Project project) {
		project.afterEvaluate { proj ->
			def bootWarTask = proj.tasks.findByName 'bootWar'
			def warTask = bootWarTask?.enabled ? bootWarTask : proj.tasks['war']

			// Now need to change the main publication to publish the war instead of the jar
			// Create a publication for the war
			proj.configure(proj.publishing) {
				publications {
					mainWar(MavenPublication) {
						artifactId 'artifactory-publish-sample'
						artifact warTask
					}
				}

				publications.removeAll { it.name == 'mainJava' }
			}

			// Re-configure the artifactoryPublish task, removing the mainJava publication & adding the mainWar one
			def artifactoryPublishTask = proj.tasks['artifactoryPublish']
			artifactoryPublishTask.mavenPublications.removeAll { it.name == 'mainJava' }
			proj.configure(artifactoryPublishTask) { publications 'mainWar' }
		}
	}
}