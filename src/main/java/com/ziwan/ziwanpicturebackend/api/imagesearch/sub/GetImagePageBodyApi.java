package com.ziwan.ziwanpicturebackend.api.imagesearch.sub;


import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetImagePageBodyApi {

    /**
     * 获取以图搜图的页面地址
     *
     * @param imageUrl 来源地址
     * @return 页面地址
     */
    public static String getImagePageUrl(String imageUrl) {


        // 搜狗识图接口
        String apiUrl = "https://ris.sogou.com/risapi/pc/sim";

        // 构造 query 参数
        String queryUrl = "https://img02.sogoucdn.com/v2/thumb/retype_exclude_gif/ext/auto"
                + "?appid=122&url=" + imageUrl;


        //        query :https://img02.sogoucdn.com/v2/thumb/retype_exclude_gif/ext/auto?appid=122&url=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2025%2F07%2F31%2F16%2F09%2Fanimal-9747276_1280.jpg
        //        start :24
        //        plevel :-1
        //1、准备请求参数
        Map<String, Object> fromData = new HashMap<>();
        fromData.put("query", queryUrl);
        fromData.put("start", "0");
        fromData.put("plevel", "-1");


        try {
            // 发送GET请求并接收响应
            HttpResponse httpResponse = HttpUtil.createGet(apiUrl)
                    .form(fromData)
                    .timeout(5000)
                    .execute();
            //!httpResponse.isOk()
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
            }


            return httpResponse.body();

        } catch (BusinessException e) {
            log.error("调用搜狗以图搜图失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
        }
    }

}
