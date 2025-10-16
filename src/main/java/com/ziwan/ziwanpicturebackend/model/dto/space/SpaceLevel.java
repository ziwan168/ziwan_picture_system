package com.ziwan.ziwanpicturebackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class SpaceLevel implements Serializable {

    // 值
    private int value;
    // 描述
    private String text;
    // 最大空间大小
    private long maxSize;
    // 最大空间数量
    private long maxCount;



    private static final long serialVersionUID = 1L;
}
