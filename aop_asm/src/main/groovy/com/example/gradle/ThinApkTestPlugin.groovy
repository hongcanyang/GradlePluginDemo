package com.example.gradle

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

public class ThinApkTestPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            println 'project(' + project.name + ') apply aop plugin'
            project.extensions.create("thinRConfig", ThinApkRExtension, project)
            def android = project.extensions.getByName("android")
            def transformImpl = new ThinAPkTransform(project)
            android.registerTransform(transformImpl)
        }
    }
}