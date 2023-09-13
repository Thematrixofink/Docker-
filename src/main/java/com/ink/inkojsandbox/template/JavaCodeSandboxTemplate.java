package com.ink.inkojsandbox.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.ink.inkojsandbox.Utils.ProcessUtils;
import com.ink.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.ink.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.ink.inkojsandbox.model.dto.ExecuteMessage;
import com.ink.inkojsandbox.model.dto.JudgeInfo;
import com.ink.inkojsandbox.service.CodeSandbox;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    //代码临时存放文件夹名字
    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";
    //代码统一类名
    private static final String GLOBAL_CODE_CLASS_NAME = "Solution.java";
    //超时时间
    private static final long TIME_OUT = 3000L;
    //用户代码的父路径
    protected static String userCodeParentPath;
    //用户代码的路径
    protected static String userCodePath;
    //黑名单代码目录
    private static final List<String> blackList = Arrays.asList("exec", "Files","File");
    //字典树
    private static final WordTree WORD_TREE;

    //静态代码块初始化文件路径
    static {
        //获取当前用户的工作目录
        String userDir = System.getProperty("user.dir");
        //使用File.separator来获取分隔符，因为不同系统的分割符不一样
        String codePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(codePathName)) {
            FileUtil.mkdir(codePathName);
        }
        //因为代码文件都叫Solution，所以肯定不能都放到一个目录下面，这里我们使用UUID来分开存储
        //代码的上一级路径
        userCodeParentPath = codePathName + File.separator + UUID.randomUUID();
        //java代码文件路径
        userCodePath = userCodeParentPath + File.separator + GLOBAL_CODE_CLASS_NAME;
        //初始化我们的字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(blackList);
    }

    /**
     * 是否有危险操作
     *
     * @param code 用户的代码
     * @return 返回结果
     */
    protected String isDangerousAction(String code) {
            //校验是否存在非法代码
            FoundWord foundWord = WORD_TREE.matchWord(code);
            if (foundWord != null) {
                return foundWord.getFoundWord();
            }
            return null;
    }

    /**
     * 把代码保存为文件Solution.java
     *
     * @param code 用户的代码
     * @return 保存之后的文件
     */
    protected File saveCodeToFile(String code) {
        //写入文件
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 编译java文件，得到Class文件
     *
     * @param userCodeFile 含有用户代码的Java文件
     * @return 执行信息
     */
    protected ExecuteMessage compileFile(File userCodeFile) {
        //2.编译代码，得到Class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        //执行编译程序
        Process compileProcess;
        try {
            compileProcess = Runtime.getRuntime().exec(compileCmd);
        } catch (IOException e) {
            //todo

            throw new RuntimeException(e);
        }
        //获取编译的信息
        ExecuteMessage compileProcessMessage = ProcessUtils.getRunProcessMessage(compileProcess, "编译");
        System.out.println("===================代码编译信息====================");
        System.out.println(compileProcessMessage);
        System.out.println("=================================================\n");
        return compileProcessMessage;
    }

    /**
     * 执行代码
     *
     * @param inputList 用户的输入
     * @return 执行信息的集合
     */
    protected List<ExecuteMessage> runCode(List<String> inputList) {
        List<ExecuteMessage> runClassProcessMessage = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Solution %s", userCodeParentPath, inputArgs);
            try {
                Process runClassCmd = Runtime.getRuntime().exec(runCmd);
                //创建一个线程，如果睡醒了,还没执行完，就给他结束了
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("到达最大运行时间，将摧毁运行代码线程（如果已经运行完毕，可忽略）");
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
                //todo
                throw new RuntimeException(e);
            }
        }
        return runClassProcessMessage;
    }

    /**
     * 整理输出
     *
     * @param runClassProcessMessage 运行后的执行信息集合
     * @return 执行代码响应类
     */
    protected ExecuteCodeResponse processOutput(List<ExecuteMessage> runClassProcessMessage) {
        //4.整理输出
        //4.1 获取输出
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //todo 设置judgeinfo的内容
        //取时采用最大值，便于判断是否超时
        long maxRunCodeTime = 0;
        JudgeInfo judgeInfo = new JudgeInfo();
        for (ExecuteMessage executeMessage : runClassProcessMessage) {
            //如果错误信息不为空，那么直接把错误结果返回去，输出
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                break;
            }
            //没有错误信息时
            //获取运行最长时间
            Long thisCodeTime = executeMessage.getTime();
            if (thisCodeTime > maxRunCodeTime) {
                maxRunCodeTime = thisCodeTime;
            }
            outputList.add(executeMessage.getNormalMessage());
            executeCodeResponse.setStatus(1);
        }
        judgeInfo.setTime(maxRunCodeTime);
        executeCodeResponse.setOutputList(outputList);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 清理代码
     *
     * @param userCodeFile 用户代码文件
     */
    protected void clearCodeFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            if (del) {
                System.out.println("删除用户代码文件夹成功!");
            } else {
                System.out.println("删除用户代码文件夹失败!");
            }
        }
    }


    /**
     * 执行代码顺序
     *
     * @param executeCodeRequest 执行代码的请求类
     * @return 返回执行响应
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        //传过来的所有参数
        String code = executeCodeRequest.getCode();             //用户的代码
        List<String> inputList = executeCodeRequest.getInput(); //用户的输入
        //todo 根据不同的语言进行不同的操作，下面默认为java
        String language = executeCodeRequest.getLanguage();     //用户代码的语言


        //0.判断代码中是否有敏感词
        String dangerousWord = isDangerousAction(code);
        if (dangerousWord != null) {
            ExecuteCodeResponse response = new ExecuteCodeResponse();
            response.setOutputList(null);
            response.setMessage("包含敏感词:"+ dangerousWord);
            response.setStatus(3);
            response.setJudgeInfo(null);
            return response;
        }

        //1.把用户的代码保存为Solution.java 文件
        File userCodeFile = saveCodeToFile(code);

        //2.编译代码，得到Class文件
        ExecuteMessage compileProcessMessage = compileFile(userCodeFile);

        //3.执行代码,获取执行信息
        List<ExecuteMessage> runClassProcessMessage = runCode(inputList);

        //4.整理输出
        ExecuteCodeResponse executeCodeResponse = processOutput(runClassProcessMessage);

        //5.文件清理,如果文件夹不为空，那么就把整个文件夹删除
        clearCodeFile(userCodeFile);

        return executeCodeResponse;
    }


    /**
     * 获取错误响应
     *
     * @param e 错误
     * @return 响应类
     */
    protected ExecuteCodeResponse getErrorResponse(Exception e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }

}
