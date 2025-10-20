package com.ziwan.ziwanpicturebackend.model.dto.picture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditByBatchRequest implements Serializable {
  
    /**  
     * 图片 id 列表  
     */  
    private List<Long> pictureIdList;
  
    /**  
     * 空间 id  
     */  
    private Long spaceId;  
  
    /**  
     * 分类  
     */  
    private String category;  
  
    /**  
     * 标签  
     */  
    private List<String> tags;

    /**
     * 命名规则
     */
    private String nameRule;
  
    private static final long serialVersionUID = 1L;  
}

