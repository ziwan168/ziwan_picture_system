package com.ziwan.ziwanpicturebackend.model.dto.picture;

import com.ziwan.ziwanpicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    @Serial
    private static final long serialVersionUID = 1L;
}

