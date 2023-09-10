package com.ink.inkojsandbox.model.dto;

import lombok.Data;

@Data
public class JudgeInfo {
    /**
     * 题目信息
     */
    private String message;
    /**
     * 消耗时间
     */
    private Long time;
    /**
     * 消耗内存
     */
    private Long memory;
}
