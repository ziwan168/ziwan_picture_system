package com.ziwan.ziwanpicturebackend.model.vo.space;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**用户上传时间区间内上传图片数量响应类
 * @author brave
 * &#064;date  2025/10/15 18:41:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeResponse implements Serializable {

    /**
     * 时间区间
     */
    private String period;

    /**
     * 上传数量
     */
    private Long count;

    @Serial
    private static final long serialVersionUID = 1L;
}

