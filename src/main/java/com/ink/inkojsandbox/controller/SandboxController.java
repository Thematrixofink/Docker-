package com.ink.inkojsandbox.controller;

import com.ink.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.ink.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.ink.inkojsandbox.service.impl.JavaNativeSandbox;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class SandboxController {

    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET_KEY = "secret";
    @Resource
    private JavaNativeSandbox javaNativeSandbox;


    /**
     * 执行代码的接口
     * @param executeCodeRequest 执行代码请求体
     * @return 返回执行的结果
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse){
        //进行基本的认证
        String authHeader = httpServletRequest.getHeader(AUTH_REQUEST_HEADER);
        if(!authHeader.equals(AUTH_REQUEST_SECRET_KEY)){
            httpServletResponse.setStatus(403);
            return null;
        }
        if(executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空!");
        }
        ExecuteCodeResponse response = javaNativeSandbox.executeCode(executeCodeRequest);
        return response;
    }

}
