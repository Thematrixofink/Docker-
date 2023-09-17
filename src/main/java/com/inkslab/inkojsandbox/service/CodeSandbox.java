package com.inkslab.inkojsandbox.service;

import com.inkslab.inkojsandbox.model.dto.ExecuteCodeRequest;
import com.inkslab.inkojsandbox.model.dto.ExecuteCodeResponse;

/**
 * 沙箱接口，定义沙箱的操作
 */
public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}

