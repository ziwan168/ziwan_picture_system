package com.ziwan.ziwanpicturebackend.model.dto.picture;

import lombok.Data;
import org.springframework.scheduling.annotation.Async;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5969271161451624168L;
    /**
     * 图片id
     */
    private Long id;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 文件名前缀
     */
    private String namePrefix;

    /**
     * 空间id
     */
    private Long spaceId;

}
