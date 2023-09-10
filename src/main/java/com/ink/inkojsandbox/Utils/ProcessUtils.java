package com.ink.inkojsandbox.Utils;


import com.ink.inkojsandbox.model.dto.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessUtils {
    /**
     * 获取运行的控制台信息
     * @return
     */
    public static ExecuteMessage getRunProcessMessage(Process process,String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            //定义计时器，开始计时
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //得到退出的状态码
            int exitNum = process.waitFor();
            executeMessage.setExitNum(exitNum);
            if (exitNum == 0) {
                //成功
                System.out.println(opName+"成功");
                //获取进程的输出
                //把process的东西输入到一个输入流里面，然后打印，所以是输入流！！！
                BufferedReader normalReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                ;
                String compileLineMessage;
                StringBuilder normalMessageBuilder = new StringBuilder();
                while ((compileLineMessage = normalReader.readLine()) != null) {
                    normalMessageBuilder.append(compileLineMessage);
                }
                executeMessage.setNormalMessage(normalMessageBuilder.toString());
                normalReader.close();
            } else {
                //失败
                System.out.println(opName+"失败!  进程运行状态码:" + exitNum);
                //获取进程的输出
                BufferedReader normalReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String compileLineMessage;
                StringBuilder normalMessageBuilder = new StringBuilder();
                while ((compileLineMessage = normalReader.readLine()) != null) {
                    normalMessageBuilder.append(compileLineMessage);
                }
                executeMessage.setNormalMessage(normalMessageBuilder.toString());
                normalReader.close();
                //获取错误流，打印错误信息
                //todo 编译错误时候，输出为乱码
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String compileErrorMessage;
                StringBuilder errorMessageBuilder = new StringBuilder();
                while ((compileErrorMessage = errorReader.readLine()) != null) {
                    errorMessageBuilder.append(compileErrorMessage);
                }
                executeMessage.setErrorMessage(errorMessageBuilder.toString());
                errorReader.close();
            }
            //计时结束
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }
}
