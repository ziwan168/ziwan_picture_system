package com.ziwan.ziwanpicturebackend.model.vo.space;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空间图片分类响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpaceCategoryAnalyzeResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -6971366083745485677L;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片数量
     */
    private Long count;

    /**
     * 分类图片总大小
     */
    private Long totalSize;

}

