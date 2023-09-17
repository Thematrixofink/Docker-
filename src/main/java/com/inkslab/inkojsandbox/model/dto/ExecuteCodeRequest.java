package com.inkslab.inkojsandbox.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
    /**
     * 用户的代码
     */
    private String code;

    /**
     * 输入用例
     */
    private List<String> input;


    /**
     * 代码语言
     */
    private String language;

    /**
     *
     */
}
