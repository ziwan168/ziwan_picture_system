package com.ziwan.ziwanpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;Description:  查询图片颜色请求
 */
@Data
public class SearchPictureByColorRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 图片颜色
     */
    private String picColor;
    /**
     * 空间id
     */
    private Long spaceId;
}
