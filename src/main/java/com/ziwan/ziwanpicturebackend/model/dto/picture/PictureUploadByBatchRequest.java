package com.ziwan.ziwanpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PictureUploadByBatchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8383758906817596096L;
    private String searchText;

    private Integer count = 10;

    private String namePrefix;



}
