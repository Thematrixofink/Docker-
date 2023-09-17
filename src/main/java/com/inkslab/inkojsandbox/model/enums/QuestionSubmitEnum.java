package com.inkslab.inkojsandbox.model.enums;



import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum QuestionSubmitEnum {

    WAITING("等待中",0),
    RUNNING("判题中",1),
    SUCCESS("成功",2),
    FAILURE("失败",3);

    private final String text;
    private final Integer value;

    QuestionSubmitEnum(String text, Integer value){
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static QuestionSubmitEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (QuestionSubmitEnum questionSubmitEnum : QuestionSubmitEnum.values()) {
            if (questionSubmitEnum.value.equals(value)) {
                return questionSubmitEnum;
            }
        }
        return null;
    }
    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
