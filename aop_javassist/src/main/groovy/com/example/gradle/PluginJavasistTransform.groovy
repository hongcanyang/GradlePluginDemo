package com.example.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

public class PluginJavasistTransform implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.getByName("android")

        //注册一个Transform
        def classTransform = new JavasistTestTransform(project)
        android.registerTransform(classTransform)
    }
}