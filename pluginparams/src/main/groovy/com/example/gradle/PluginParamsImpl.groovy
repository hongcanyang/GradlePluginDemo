package com.example.gradle

import com.android.build.gradle.AppPlugin
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.*
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle;

class MyCustomPluginExtension {
    String message = "From MyCustomPluginExtention"
//    def sender = "MyCustomPluin"
}

class PluginParamsImpl implements Plugin<Project> {

    def TAG = 'PluginParamsImpl'
    void apply(Project project) {

//        printLifeCycle(project)

        project.extensions.create('myArgs', MyCustomPluginExtension)

        project.task('paramsTask') {
            def args = project.extensions.getByName("myArgs")
            println "Sender --- is ${args.message}"
        }

//        project.getTasks().create('test3', PluginParamsTask.class, new Action<PluginParamsTask>() {
//            @Override
//            void execute(PluginParamsTask t) {
//
//                t.doFirst {
//                    println("init")
//                }
//                t.output()
//
//                t.doLast {
//                    println("release")
//                }
//            }
//        })
    }

    public void printLifeCycle(Project project) {
        project.gradle.beforeProject {p ->
            println(TAG + '  #### --- beforeProject ---')
        }

        project.gradle.afterProject {p ->
            println(TAG + '  #### --- afterProject ---')
        }


        project.beforeEvaluate { pro
            println TAG + "  #### Evaluate before of "+ pro.path
        }

        project.afterEvaluate { pro ->
            println(TAG + "  #### Evaluate after of " + pro.path)
        }

        project.gradle.buildFinished { r ->
            println(TAG + "  #### buildFinished "+r.failure)
        }

        project.gradle.projectsLoaded {gradle ->
            println(TAG + "  #### projectsLoaded")
        }

        project.gradle.addBuildListener(new BuildListener() {
            @Override
            void buildStarted(Gradle gradle) {
                println(TAG + "  ### gradle.buildStarted")
            }

            @Override
            void settingsEvaluated(Settings settings) {
                println(TAG + "  ### gradle.settingsEvaluated")
            }

            @Override
            void projectsLoaded(Gradle gradle) {
                println(TAG + "  ### gradle.projectsLoaded")
            }
            @Override
            void projectsEvaluated(Gradle gradle) {
                println(TAG + "  ### gradle.projectsEvaluated")
            }
            @Override
            void buildFinished(BuildResult result) {
                println(TAG + "  ### gradle.buildFinished")
            }
        })
    }
}



