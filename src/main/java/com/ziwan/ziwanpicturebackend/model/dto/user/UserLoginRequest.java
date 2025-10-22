package com.ziwan.ziwanpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = 503403622223050286L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

}


