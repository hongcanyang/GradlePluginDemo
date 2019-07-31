package com.example.gradle

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import java.util.regex.Pattern

public class ThinApkRExtension {

    NamedDomainObjectContainer<KeepRInfo> keepInfo

    private static final String TAG = 'ThinApkRExtension'

    ThinApkRExtension(Project project) {
        keepInfo = project.container(KeepRInfo)
    }

    void printKeepInfo() {
        if (keepInfo != null) {
            keepInfo.each { KeepRInfo keepRInfo ->
                keepRInfo.printInfo()
            }
        }
    }

    void keepInfo(Action<NamedDomainObjectContainer<KeepRInfo>> action) {
        action.execute(keepInfo)
    }

    /**
     * 该R文件是否要keep住
     *
     * @param className  eg：com/example/gradledemo/R$mipmap.class
     * @return
     */
    KeepRInfo shouldKeepRFile(String className) {
        KeepRInfo keep = null
        keepInfo.each { KeepRInfo keepRInfo ->

            if ((keepRInfo.getRClassStr() + ".class").equals(className)) {
                println('---  className : ' + className + ' keepInfo ' + (keepRInfo.getRClassStr() + ".class"))
                keep = keepRInfo
            }
        }
        return keep
    }


    static class KeepRInfo {
        String name
        //需要keep的包名，例如：com.example.gradledemo
        String keepRPackageName
        //需要keep的类名，例如 mipmap、drawable
        String keepRClassName
        //需要keep的资源名
        List<String> keepResName
        //需要keep的资源名正则表达式
        List<String> keepResNameReg

        private String rClassStr

        KeepRInfo(String name) {
            this.name = name
        }

        boolean shouldKeep(String fieldName) {
            if (keepResName != null && !keepResName.isEmpty()) {
                for (String name : keepResName) {
                    if (name == fieldName) {
                        return true
                    }
                }
            }
            if (keepResNameReg != null && !keepResNameReg.isEmpty()) {
                for (String reg : keepResNameReg) {
                    if (reg == null || reg == "")
                        continue
                    if (isMatch(fieldName, reg)) {
                        return true
                    }
                }
            }
            return false
        }

        String getRClassStr() {
            if (rClassStr == null) {
                rClassStr = "${keepRPackageName.replace(".", "/")}/R\$${keepRClassName}"
            }
            return rClassStr
        }

        boolean isMatch(String name, String reg) {
            Pattern pattern = Pattern.compile(reg)
            pattern.matcher(name).matches()
        }

        void printInfo() {
            println TAG + " --- packageName = ${keepRPackageName}, className = ${keepRClassName}"
            if (keepResName != null) {
                for (String name : keepResName) {
                    println TAG + " --- keep res name: ${name}"
                }
            }
            println TAG + " --- RClass = ${getRClassStr()}"
        }
    }

}
