package com.ziwan.ziwanpicturebackend.model.enums;

import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import lombok.Getter;


@Getter
public enum TimeDimensionEnum {

    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String value;

    TimeDimensionEnum(String value) {
        this.value = value;
    }


    public static TimeDimensionEnum getEnumByValue(String value) {
        for (TimeDimensionEnum item : TimeDimensionEnum.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "时间维度不存在");
    }


}


