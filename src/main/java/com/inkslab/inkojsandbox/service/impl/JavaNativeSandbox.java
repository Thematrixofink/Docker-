package com.inkslab.inkojsandbox.service.impl;

import com.inkslab.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.inkslab.inkojsandbox.model.dto.ExecuteCodeResponse;
import com.inkslab.inkojsandbox.template.JavaCodeSandboxTemplate;
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
