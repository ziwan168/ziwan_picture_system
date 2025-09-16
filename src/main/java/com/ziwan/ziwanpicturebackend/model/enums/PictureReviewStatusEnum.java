package com.ziwan.ziwanpicturebackend.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片审核状态枚举
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEW_WAITING("待审核", 0),
    REVIEW_PASS("通过", 1),
    REVIEW_REJECT("拒绝", 2);

    private final String text;
    private final int value;

    // 静态 Map，用于 value -> 枚举快速查找
    private static final Map<Integer, PictureReviewStatusEnum> VALUE_MAP = new HashMap<>();

    static {
        for (PictureReviewStatusEnum e : PictureReviewStatusEnum.values()) {
            VALUE_MAP.put(e.getValue(), e);
        }

    }

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取对应枚举实例
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
    }
}

