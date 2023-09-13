package com.ink.inkojsandbox.controller;

import com.ink.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.ink.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.ink.inkojsandbox.service.impl.JavaNativeSandbox;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/")
public class SandboxController {
    @Resource
    private JavaNativeSandbox javaNativeSandbox;


    /**
     * 执行代码的接口
     * @param executeCodeRequest 执行代码请求体
     * @return 返回执行的结果
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest){
        if(executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空!");
        }
        return javaNativeSandbox.executeCode(executeCodeRequest);
    }

}
