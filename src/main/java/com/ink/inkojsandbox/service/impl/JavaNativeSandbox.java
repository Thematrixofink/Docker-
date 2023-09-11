package com.ink.inkojsandbox.service.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.ink.inkojsandbox.Utils.ProcessUtils;
import com.ink.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.ink.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.ink.inkojsandbox.model.dto.ExecuteMessage;
import com.ink.inkojsandbox.model.dto.JudgeInfo;
import com.ink.inkojsandbox.security.DefaultSecurityManager;
import com.ink.inkojsandbox.service.CodeSandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Java实现代码沙箱
 */
public class JavaNativeSandbox implements CodeSandbox {

    //测试以下
    public static void main(String[] args) {
        JavaNativeSandbox javaNativeSandbox = new JavaNativeSandbox();
        ExecuteCodeRequest codeRequest = new ExecuteCodeRequest();
        codeRequest.setCode("public class Solution {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        try {\n" +
                "            Thread.sleep(1000000L);\n" +
                "        } catch (InterruptedException e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }\n" +
                "        System.out.println(\"exec\");\n" +
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

    //代码临时存放文件夹名字
    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";
    //代码统一类名
    private static final String GLOBAL_CODE_CLASS_NAME = "Solution.java";
    //超时时间
    private static final long TIME_OUT = 5000L;
    //黑名单代码目录
    private static final List<String> blackList = Arrays.asList("exec","Files");

    private static final WordTree WORD_TREE;

    static{
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        //传过来的所有参数
        String code = executeCodeRequest.getCode();             //用户的代码
        List<String> inputList = executeCodeRequest.getInput(); //用户的输入
        //todo 根据不同的语言进行不同的操作，下面默认为java
        String language = executeCodeRequest.getLanguage();     //用户代码的语言

        //校验是否存在非法代码
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if(foundWord != null){
            System.out.println("包含敏感词:" + foundWord.getFoundWord());
            return null;
        }

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
        Process compileProcess;
        try {
            compileProcess = Runtime.getRuntime().exec(compileCmd);
        } catch (IOException e) {
            return getErrorResponse(e);
        }
        //获取编译的信息
        ExecuteMessage compileProcessMessage = ProcessUtils.getRunProcessMessage(compileProcess,"编译");
        System.out.println("===================代码编译信息====================");
        System.out.println(compileProcessMessage);
        System.out.println("=================================================\n");

        //3.执行代码,获取执行信息
        List<ExecuteMessage> runClassProcessMessage = new ArrayList<>();
        for(String inputArgs : inputList){
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Solution %s",userCodeParentPath,inputArgs);
            try {
                Process runClassCmd = Runtime.getRuntime().exec(runCmd);
                //创建一个线程，如果睡醒了,还没执行完，就给他结束了
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("运行超时！");
                        runClassCmd.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                ExecuteMessage runProcessMessage = ProcessUtils.getRunProcessMessage(runClassCmd, "运行");
                runClassProcessMessage.add(runProcessMessage);
                System.out.println("===================代码运行信息====================");
                System.out.println(runProcessMessage);
                System.out.println("================================================\n");
            } catch (IOException e) {
                return getErrorResponse(e);
            }
        }

        //4.整理输出
        //4.1 获取输出
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //todo 设置judgeinfo的内容
        //取时采用最大值，便于判断师是否超时
        long maxRunCodeTime = 0;
        JudgeInfo judgeInfo = new JudgeInfo();
        for (ExecuteMessage executeMessage : runClassProcessMessage) {
            //如果错误信息不为空，那么直接把错误结果返回去，输出
            String errorMessage = executeMessage.getErrorMessage();
            if(StrUtil.isNotBlank(errorMessage)){
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                break;
            }
            //没有错误信息时
            //获取运行最长时间
            Long thisCodeTime = executeMessage.getTime();
            if(thisCodeTime > maxRunCodeTime){
                maxRunCodeTime = thisCodeTime;
            }
            outputList.add(executeMessage.getNormalMessage());
            executeCodeResponse.setStatus(1);
        }
        judgeInfo.setTime(maxRunCodeTime);
        executeCodeResponse.setOutputList(outputList);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        //5.文件清理,如果文件夹不为空，那么就把整个文件夹删除
        if(userCodeFile.getParentFile() != null){
            boolean del = FileUtil.del(userCodeParentPath);
            if(del) { System.out.println("删除用户代码文件夹成功!");}
            else    { System.out.println("删除用户代码文件夹失败!");}
        }
        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Exception e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
