package com.inkslab.inkojsandbox.service.impl;


import com.inkslab.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.inkslab.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.inkslab.inkojsandbox.service.CodeSandbox;

/**
 * 第三方代码沙箱
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
