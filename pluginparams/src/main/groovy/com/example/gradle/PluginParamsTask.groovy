package com.example.gradle

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

class PluginParamsTask extends DefaultTask {

    private static final String TAG = 'PluginParamsTask'

    @Override
    Task doFirst(Action<? super Task> action) {
        println(TAG + "- doFirst")
        return super.doFirst(action)
    }

    @Override
    Task doLast(Action<? super Task> action) {
        println(TAG + "- doLast")
        return super.doLast(action)
    }


    @TaskAction
    void output() {
        println "Sender is ${project.myArgs.sender} \nmessage: ${project.myArgs.message}"
    }


}