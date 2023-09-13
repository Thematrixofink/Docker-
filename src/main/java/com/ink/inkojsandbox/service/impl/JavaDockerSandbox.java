package com.ink.inkojsandbox.service.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.ink.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.ink.inkojsandbox.model.dto.ExecuteMessage;
import com.ink.inkojsandbox.model.dto.JudgeInfo;
import com.ink.inkojsandbox.model.enums.JudgeInfoEnum;
import com.ink.inkojsandbox.template.JavaCodeSandboxTemplate;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Java控制docker实现代码沙箱
 */
public class JavaDockerSandbox extends JavaCodeSandboxTemplate {




    //超时时间
    private static final long TIME_OUT = 5000L;

    private static Boolean FIRST_RUN = true;

    private static final boolean[] isTimeOut = {true};


    /**
     * 通过docker实现代码的运行
     * @param inputList 用户的输入
     * @return 返回运行的结果
     */
    @Override
    protected List<ExecuteMessage> runCode(List<String> inputList) {
        //3.拉取镜像，创建容器
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String image = "openjdk:8-alpine";
        //只有第一次运行的时候才需要拉取镜像
        if (FIRST_RUN) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像:" + item.getStatus());
                    FIRST_RUN = false;
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
                System.out.println("下载镜像完成");
                pullImageCmd.close();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像失败!");
                pullImageCmd.close();
                throw new RuntimeException(e);
            }
        }
        //创建容器
        HostConfig hostConfig = new HostConfig();
        //创建容器时，可以指定文件路径，将本地的文件同步到容器中，可以让容器访问（数据卷)
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        hostConfig.withMemory(100 * 1000 * 1000L);
        //内存交换为0，一定程度可以保证程序的稳定
        hostConfig.withMemorySwap(0L);

        //todo 完善配置参数
        //hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));

        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(image);
        CreateContainerResponse createContainerResponse = createContainerCmd
                .withHostConfig(hostConfig)
                //网络金庸
                .withNetworkDisabled(true)
                //确保容器能够交互
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();
        System.out.println("创建容器成功,ID为:" + containerId);
        createContainerCmd.close();


        //启动容器
        dockerClient.startContainerCmd(containerId).exec();
        List<ExecuteMessage> runClassProcessMessage = new ArrayList<>();
        //传入参数，执行代码，并获取参数

        for (String input : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] params = input.split(" ");
            String[] cmdArray = ArrayUtil.append( new String[]{"java", "-cp", "/app", "Solution"},params);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令:" + execCreateCmdResponse);
            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] normalMessage = {null};
            final String[] errorMessage = {null};
            String executeId = execCreateCmdResponse.getId();

            //定义callback，定义输出结果的去向
            ExecStartResultCallback executeStartResultCallback = new ExecStartResultCallback(){
                //如果TIME_OUT时间内执行完毕，就会执行这个方法，把是否超时变量设置为flase
                @Override
                public void onComplete() {
                    isTimeOut[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if(StreamType.STDERR.equals(streamType)){
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.printf("输出错误结果:" + errorMessage[0]);

                    }else{
                        normalMessage[0] = new String(frame.getPayload());
                        System.out.printf("输出结果:"+ normalMessage[0]);
                    }
                    super.onNext(frame);
                }
            };
            final long[] maxMemory = {0L};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);

            //进行内存检测
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用:" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            });
            statsCmd.exec(statisticsResultCallback);
            statsCmd.close();

            //执行代码
            try {
                stopWatch.start();
                dockerClient.execStartCmd(executeId)
                        .exec(executeStartResultCallback)
                        .awaitCompletion(TIME_OUT,TimeUnit.MILLISECONDS);
                stopWatch.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //获取输出结果
            long thisCodeTime = stopWatch.getLastTaskTimeMillis();
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setNormalMessage(normalMessage[0]);
            executeMessage.setTime(thisCodeTime);
            executeMessage.setMemory(maxMemory[0]);
            runClassProcessMessage.add(executeMessage);
        }
        //这里直接杀死
        KillContainerCmd killContainerCmd = dockerClient.killContainerCmd(containerId);
        killContainerCmd.exec();
        System.out.println("关闭容器成功!");
        RemoveContainerCmd removeContainerCmd = dockerClient.removeContainerCmd(containerId);
        removeContainerCmd.exec();
        System.out.println("删除容器成功!");
        return runClassProcessMessage;
    }

    /**
     * 相比于模板方法，实现了内存监测
     * @param runClassProcessMessage 运行后的执行信息集合
     * @return 返回执行代码响应类
     */
    @Override
    protected ExecuteCodeResponse processOutput(List<ExecuteMessage> runClassProcessMessage) {
        //整理文件输出
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //取时采用最大值，便于判断师是否超时
        long maxRunCodeTime = 0;
        long maxMemory = 0;
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
            Long thisCodeMemory = executeMessage.getMemory();
            if(thisCodeTime > maxRunCodeTime){
                maxRunCodeTime = thisCodeTime;
            }
            if(thisCodeMemory > maxMemory){
                maxMemory = thisCodeMemory;
            }
            outputList.add(executeMessage.getNormalMessage());
            executeCodeResponse.setStatus(1);
        }
        judgeInfo.setTime(maxRunCodeTime);
        judgeInfo.setMemory(maxMemory);
        //判断是否超时
        if(isTimeOut[0]){
            judgeInfo.setMessage(JudgeInfoEnum.TIME_LIMIT_EXCEEDED.getValue());
        }
        executeCodeResponse.setOutputList(outputList);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }



}
