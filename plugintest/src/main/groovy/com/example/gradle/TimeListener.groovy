package com.example.gradle

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class TimeListener implements TaskExecutionListener, BuildListener {

    private long startTime
    private List<String> mTimeList = new ArrayList<>()

    @Override
    void buildStarted(Gradle gradle) {

    }

    @Override
    void settingsEvaluated(Settings settings) {

    }

    @Override
    void projectsLoaded(Gradle gradle) {

    }

    @Override
    void projectsEvaluated(Gradle gradle) {

    }

    @Override
    void buildFinished(BuildResult buildResult) {
        println "Task spend time:"
        for (time in mTimeList) {
            println time
        }
    }

    @Override
    void beforeExecute(Task task) {

        startTime = System.currentTimeMillis()

    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        def ms = System.currentTimeMillis() - startTime
        String item = ms + " ms - " + task.path
        mTimeList.add(item)
        task.project.logger.warn "${task.path} spend ${ms}ms"
    }
}