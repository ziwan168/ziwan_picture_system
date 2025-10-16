package com.ziwan.ziwanpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum SpaceLevelEnum {

    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    private final String text;
    private final int value;
    private final long maxCount;
    private final long maxSize;

    /**
     * @param text 文本
     * @param value 值
     * @param maxCount 最大图片总数量
     * @param maxSize 最大图片总大小
     */
    SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    private static final Map<Integer, SpaceLevelEnum> CACHE =
            Arrays.stream(values()).collect(Collectors.toMap(SpaceLevelEnum::getValue, e -> e));

    /**
     * 根据 value 获取枚举
     */
    public static SpaceLevelEnum getEnumByValue(Integer value) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(value), ErrorCode.PARAM_ERROR, "空间级别参数为空");
        SpaceLevelEnum result = CACHE.get(value);
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的空间级别值：" + value);
        }
        return result;
    }
}


