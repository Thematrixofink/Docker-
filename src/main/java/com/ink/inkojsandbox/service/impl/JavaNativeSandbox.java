package com.ink.inkojsandbox.service.impl;

import cn.hutool.core.io.FileUtil;
import com.ink.inkojsandbox.Utils.ProcessUtils;
import com.ink.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.ink.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.ink.inkojsandbox.model.dto.ExecuteMessage;
import com.ink.inkojsandbox.service.CodeSandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Java实现代码沙箱
 */
public class JavaNativeSandbox implements CodeSandbox {

    public static void main(String[] args) {
        JavaNativeSandbox javaNativeSandbox = new JavaNativeSandbox();
        ExecuteCodeRequest codeRequest = new ExecuteCodeRequest();
        codeRequest.setCode("public class Soluton {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(a+b);\n" +
                "    }\n" +
                "}");
        ArrayList<String> input = new ArrayList<>();
        input.add("1 2");
        input.add("2 2");
        codeRequest.setInput(input);
        codeRequest.setLanguage("java");

        javaNativeSandbox.executeCode(codeRequest);
    }

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    private static final String GLOBAL_CODE_CLASS_NAME = "Solution.java";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        //传过来的所有参数
        String code = executeCodeRequest.getCode();             //用户的代码
        List<String> inputList = executeCodeRequest.getInput(); //用户的输入
        //todo 根据不同的语言进行不同的操作，下面默认为java
        String language = executeCodeRequest.getLanguage();     //用户代码的语言


        //1.把用户的代码保存为Solution.java 文件
        //获取当前用户的工作目录
        String userDir = System.getProperty("user.dir");
        //使用File.separator来获取分隔符，因为不同系统的分割符不一样
        String codePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if(!FileUtil.exist(codePathName)){
            FileUtil.mkdir(codePathName);
        }
        //因为代码文件都叫Solution，所以肯定不能都放到一个目录下面，这里我们使用UUID来分开存储
        //代码的上一级路径
        String userCodeParentPath = codePathName + File.separator + UUID.randomUUID();
        //java代码文件路径
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_CODE_CLASS_NAME;
        //写入文件
        File userCodeFile = FileUtil.writeString(code, userCodePath , StandardCharsets.UTF_8);


        //2.编译代码，得到Class文件
        String compileCmd = String.format("javac -encoding utf-8 %s",userCodeFile.getAbsolutePath());
        //执行编译程序
        Process compileProcess = null;
        try {
            compileProcess = Runtime.getRuntime().exec(compileCmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //获取编译的信息
        ExecuteMessage compileProcessMessage = ProcessUtils.getRunProcessMessage(compileProcess,"编译");
        System.out.println(compileProcessMessage);

        //3.执行代码,获取执行信息
        ExecuteMessage runClassProcessMessage;
        for(String inputArgs : inputList){
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s Solution %s",userCodeParentPath,inputArgs);
            try {
                Process runClassCmd = Runtime.getRuntime().exec(runCmd);
                 runClassProcessMessage = ProcessUtils.getRunProcessMessage(runClassCmd,"运行");
                System.out.println(runClassProcessMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}
