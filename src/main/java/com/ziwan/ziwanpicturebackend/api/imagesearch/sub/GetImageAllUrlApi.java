package com.ziwan.ziwanpicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.ziwan.ziwanpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GetImageAllUrlApi {

    public static List<ImageSearchResult> getImageAllUrl(String body) {
        //3、处理响应
//            "status": 0,
//            "info": "ok",
//            "data": {
//                "filtered_num": 1,
//                "items": [
//                        "thumbUrl": "https://i03piccdn.sogoucdn.com/397acd758f7033d9",
//                        "size": "83k",
//                        "summarytype": "NormalSummary",
//                        "thumb_height": "209",
//                        "thumb_width": "313",
//                {

        try {
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);

            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            ThrowUtils.throwIf(data == null, ErrorCode.SYSTEM_ERROR, "没找到数据");
            Object items = data.get("items");
            JSONArray itemsArray = JSONUtil.parseArray(items);


            List<ImageSearchResult> results = new ArrayList<>();
            // 遍历 itemsArray
            // 判断 itemsArray 是否为空,不为空取到所有的缩略图
            if (itemsArray == null || itemsArray.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
            } else {
                for (Object obj : itemsArray) {
                    // 转成 HashMap
                    Map<String, Object> item = JSONUtil.toBean(obj.toString(), HashMap.class);


                    // 只取 thumbUrl
                    String thumbUrl = (String) item.get("thumbUrl");
                    if (StrUtil.isNotBlank(thumbUrl)) {
                        ImageSearchResult searchResult = new ImageSearchResult();
                        searchResult.setThumbUrl(thumbUrl);
                        results.add(searchResult);
                    }
                }
                return results;
            }
        } catch (BusinessException e) {
            log.error("调用搜狗以图搜图失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
        }
    }
}






