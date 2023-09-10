package com.ink.inkojsandbox.model.enums;

import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题消息枚举类
 */
public enum JudgeInfoEnum {

    ACCEPTED("成功","成功"),
    WRONG_ANSWER("答案错误","答案错误"),
    COMPILE_ERROR("编译错误","编译错误"),
    MEMORY_LIMIT_EXCEEDED("内存超出限制","内存超出限制"),
    TIME_LIMIT_EXCEEDED("超时","超时"),
    OUTPUT_EXCEEDED("输出超出限制","输出超出限制"),
    DANGEROUS_OPERATION("危险操作","危险操作"),
    RUNTIME_ERROR("运行错误","运行错误"),
    SYSTEM_ERROR("系统错误","系统错误");
    private final String text;

    private final String value;

    JudgeInfoEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeInfoEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (JudgeInfoEnum anEnum : JudgeInfoEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
