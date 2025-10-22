package com.ziwan.ziwanpicturebackend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class PictureTagCategory implements Serializable {

    @Serial
    private static final long serialVersionUID = -2092665456242842098L;
    private List<String> tagList;
    private List<String> categoryList;


}
