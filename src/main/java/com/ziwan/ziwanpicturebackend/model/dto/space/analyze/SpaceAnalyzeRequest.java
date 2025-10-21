package com.ziwan.ziwanpicturebackend.model.dto.space.analyze;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间分析请求类
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2468568140811548286L;
    /**
     * 空间ID
     */
    private Long spaceId;
    /**
     * 是否查询公开的
     */
    private boolean queryPublic;
    /**
     * 是否查询全部的（包括私密的）
     */
    private boolean queryAll;


}
