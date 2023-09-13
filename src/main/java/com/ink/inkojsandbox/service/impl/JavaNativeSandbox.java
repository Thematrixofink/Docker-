package com.ink.inkojsandbox.service.impl;

import com.ink.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.ink.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.ink.inkojsandbox.template.JavaCodeSandboxTemplate;
import org.springframework.stereotype.Component;

/**
 * Java 原生实现代码沙箱
 */
@Component
public class JavaNativeSandbox extends JavaCodeSandboxTemplate {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
