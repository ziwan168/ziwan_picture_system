package com.ziwan.ziwanpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {
    USER("user", "普通用户"),
    ADMIN("admin", "管理员");

    private final String role;
    private final String value;

    // 静态 Map，用于 value -> 枚举快速查找
    private static final Map<String, UserRoleEnum> VALUE_MAP = new HashMap<>();

    static {
        for (UserRoleEnum e : UserRoleEnum.values()) {
            VALUE_MAP.put(e.getRole(), e);
        }
    }

    UserRoleEnum(String role, String value) {
        this.role = role;
        this.value = value;
    }

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

