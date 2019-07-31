package com.example.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project


public class PluginImpl implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        project.gradle.addListener(new TimeListener())
        project.task("testTask")  {
            println "Hello gradle plugin 123"
        }

    }
}