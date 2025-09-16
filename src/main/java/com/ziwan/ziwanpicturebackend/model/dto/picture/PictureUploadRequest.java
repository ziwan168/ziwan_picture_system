package com.ziwan.ziwanpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = -5969271161451624168L;
    private Long id;

    private String fileUrl;

    private String namePrefix;

}
