package com.example.gradle

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.utils.FileUtils
import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import javassist.CtNewConstructor
import javassist.CtNewMethod
import javassist.Modifier
import javassist.NotFoundException
import org.gradle.api.Project

public class JavasistTestTransform extends Transform {

    private Project mProject

    public JavasistTestTransform(Project project) {
        this.mProject = project
    }


    @Override
    String getName() {
        return JavasistTestTransform.simpleName
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        HashSet<QualifiedContent.ContentType> set = new HashSet<QualifiedContent.ContentType>()
        set.add(QualifiedContent.DefaultContentType.RESOURCES)
        set.add(QualifiedContent.DefaultContentType.CLASSES)
        return set
    }

    //    指Transform要操作内容的范围，官方文档Scope有7种类型：
//
//    EXTERNAL_LIBRARIES        只有外部库
//    PROJECT                       只有项目内容
//    PROJECT_LOCAL_DEPS            只有项目的本地依赖(本地jar)
//    PROVIDED_ONLY                 只提供本地或远程依赖项
//    SUB_PROJECTS              只有子项目。
//    SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
//    TESTED_CODE                   由当前变量(包括依赖项)测试的代码
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        HashSet<QualifiedContent.Scope> set = new HashSet<QualifiedContent.Scope>()
        set.add(QualifiedContent.Scope.PROJECT)
        set.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
        set.add(QualifiedContent.Scope.SUB_PROJECTS)
        return set
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean b) throws IOException, TransformException, InterruptedException {

        println("----------------进入transform了--------------")

        // 打印class路径
//        testPrintPath(inputs, referencedInputs)
//        // 插入代码
        testInsertCode(inputs)
//
        testCreateClass(inputs, outputProvider)


        println("--------------结束transform了----------------")
    }

    void testInsertCode(Collection<TransformInput> inputs) {
        inputs.each { TransformInput input ->
            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //注入代码
                MyInjects.inject(directoryInput.file.absolutePath, mProject)
            }
        }
    }

    void testCreateClass(Collection<TransformInput> inputs, TransformOutputProvider outputProvider) {
        inputs.each { TransformInput input ->
            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                JavasistManager.createPersonClass(directoryInput.file.absolutePath, mProject)

                JavasistManager.createStudentClass(directoryInput.file.absolutePath)

//                 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY) //这里写代码片

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            ////遍历jar文件 对jar不操作，但是要输出到out路径
//            input.jarInputs.each { JarInput jarInput ->
//                // 重命名输出文件（同目录copyFile会冲突）
//                def jarName = jarInput.name
//                println("jar = " + jarInput.file.getAbsolutePath())
//                DigestUtils
//                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
//                if (jarName.endsWith(".jar")) {
//                    jarName = jarName.substring(0, jarName.length() - 4)
//                }
//                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                FileUtils.copyFile(jarInput.file, dest)
//            }
        }
    }

    void testPrintPath(Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs) {
        inputs.each { TransformInput input ->
            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dir = new File(directoryInput.file.absolutePath)
                if (dir.isDirectory()) {
                    dir.eachFileRecurse { File file ->
                        String filePath = file.absolutePath

                        println("inputs file path : " + filePath)
                    }
                }
            }
        }

        referencedInputs.each { TransformInput referencedInput ->
            referencedInput.directoryInputs.each { DirectoryInput directoryInput ->

                File dir = new File(directoryInput)
                if (dir.isDirectory()) {
                    dir.eachFileRecurse { File file ->
                        String filePath = file.absolutePath

                        println("referencedInputs file path : " + filePath)
                    }
                }
            }
        }
    }
}

public class MyInjects {
    //初始化类池
    private final static ClassPool pool = ClassPool.getDefault();

    public static void inject(String path, Project project) {
        //将当前路径加入类池,不然找不到这个类
        pool.appendClassPath(path);
        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString());
        //引入android.os.Bundle包，因为onCreate方法参数有Bundle
        pool.importPackage("android.os.Bundle");

        File dir = new File(path);
        if (dir.isDirectory()) {
            //遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                println("filePath = " + filePath)
                if (file.getName().equals("MainActivity.class")) {

                    //获取MainActivity.class
                    CtClass ctClass = pool.getCtClass("com.example.gradledemo.MainActivity");
                    //解冻
                    if (ctClass.isFrozen())
                        ctClass.defrost()

                    //获取到OnCreate方法
                    CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")

                    println("方法名 = " + ctMethod)

                    String insetBeforeStr = """ android.widget.Toast.makeText(this,"我是被插入的Toast代码~!!",android.widget.Toast.LENGTH_SHORT).show();
                    """
                    //在方法开头插入代码
                    ctMethod.insertBefore(insetBeforeStr);
                    ctClass.writeFile(path)
                    ctClass.detach()//释放
                }
            }
        }

    }

}

public class JavasistManager {
    //初始化类池

    public static CtClass createPersonClass(String path, Project project) {

        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath

//                println("file path : " + filePath)
                if (filePath.endsWith(".class")
                        && filePath.contains('MainActivity')) {
                    String className = "com.example.gradledemo.transform.Person"
                    ClassPool pool = ClassPool.getDefault();

                    pool.importPackage("")
                    CtClass cc = pool.makeClass(className);
                    if (cc.isFrozen())
                        cc.defrost()

                    try {
                        CtField identifyIdField = CtField.make("String identifyId;", cc);
                        cc.addField(identifyIdField);

                        CtField nameField = CtField.make("String name;", cc);
                        cc.addField(nameField);

                        CtField ageField = CtField.make("int age;", cc);
                        cc.addField(ageField);


                        CtConstructor con = CtNewConstructor.make(null, null, "{this" +
                                ".identifyId = \"1234567890\";}", cc);
                        cc.addConstructor(con);

                        CtMethod updateAgeMethod = CtNewMethod.abstractMethod(CtClass.voidType, "updateAge", null, null, cc);
                        cc.addMethod(updateAgeMethod);

                        CtMethod setNameMethod = CtNewMethod.make("public void setName(String name) {\n" +
                                "    this.name = name;\n" +
                                "}", cc);

                        cc.addMethod(setNameMethod);

                        cc.stopPruning(true)
                        cc.writeFile(path);

                        return cc;
                    } catch (CannotCompileException e) {
                        e.printStackTrace();
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static CtClass createStudentClass(String path) {
        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath

                if (filePath.endsWith(".class")
                        && filePath.contains('MainActivity')) {
                    String className = "com.example.gradledemo.transform.Student"
                    ClassPool pool = ClassPool.getDefault();


                    CtClass cc = pool.makeClass(className);
                    if (cc.isFrozen())
                        cc.defrost()

                    try {
                        CtClass personClass = pool.getCtClass("com.example.gradledemo.transform.Person");
                        println("personClass " + personClass)

                        if (personClass != null && personClass.isFrozen()) {
                            personClass.defrost()
                        }
                        cc.setSuperclass(personClass)
                        CtField schoolField = CtField.make("String school;", cc);
                        cc.addField(schoolField);

                        CtConstructor con = CtNewConstructor.make(null, null, null, cc);
                        cc.addConstructor(con);


                        CtMethod updateAgeMethod = CtNewMethod.make(CtClass.voidType, "updateAge", null, null, null, cc)
                        cc.addMethod(updateAgeMethod);

                        CtMethod setNameMethod = CtNewMethod.make("public void setName(String school) {\n" +
                                "    this.school = school;\n" +
                                "}", cc);

                        cc.addMethod(setNameMethod);



                        cc.writeFile(path);
                        return cc;
                    } catch (CannotCompileException e) {
                        e.printStackTrace();
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
