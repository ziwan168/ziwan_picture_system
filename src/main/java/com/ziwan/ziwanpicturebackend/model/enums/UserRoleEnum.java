package com.ziwan.ziwanpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {
    USER("user", "普通用户"),
    ADMIN("admin", "管理员");

    private final String role;
    private final String value;



    UserRoleEnum(String role, String value) {
        this.role = role;
        this.value = value;
    }

    // 用于 value -> 枚举快速查找
    private static final Map<String, UserRoleEnum> VALUE_MAP
            = Arrays.stream(values()).collect(Collectors.toMap(UserRoleEnum::getRole, e -> e));


    /**
     * 根据 value 获取对应枚举实例
     */
    public static UserRoleEnum getValueByRole(String role) {
        if (ObjUtil.isEmpty(role)) {
            return null;
        }
        return VALUE_MAP.get(role);
    }
}

