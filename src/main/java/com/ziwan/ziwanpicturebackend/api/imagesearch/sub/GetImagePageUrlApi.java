package com.ziwan.ziwanpicturebackend.api.imagesearch.sub;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取以图搜图的页面地址
     *
     * @param imageUrl 来源地址
     * @return 页面地址
     */
    public static String getImagePageUrl(String imageUrl) {

//        image :https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2016%2F09%2F29%2F19%2F55%2Fdoctor-1703644_640.jpg
//        tn :pc
//        from :pc
//        image_source :PC_UPLOAD_URL
//        sdkParams:{"data":""}

        //1、准备请求参数
        Map<String, Object> fromData = new HashMap<>();
        fromData.put("image", imageUrl);
        fromData.put("tn", "pc");
        fromData.put("from", "pc");
        fromData.put("image_source", "PC_UPLOAD_URL");
        //获取当前时间戳
        long uptime = System.currentTimeMillis();
        //请求地址：https://graph.baidu.com/upload?uptime=1760775622484
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;


        try {
            //2、发送请求
            HttpResponse httpResponse = HttpRequest.post(url)
                    .form(fromData)
                    .timeout(5000)
                    .execute();
            //!httpResponse.isOk()
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
            }
            //3、处理响应
            //{"status":0,
            // "msg":"Success",
            // "data":{"url":"https://graph.baidu.com/s?card_key=\u0026entrance=GENERAL\u0026extUiData%5BisLogoShow%5D=1\u0026f=all\u0026isLogoShow=1\u0026session_id=11512359694045817469\u0026sign=1267beb2b0c1dbe84552301760775794\u0026tpl_from=pc",
            //         "sign":"1267beb2b0c1dbe84552301760775794"}}

            log.error("百度接口响应状态码: {}", httpResponse.getStatus());
            log.error("百度接口响应内容: {}", httpResponse.body());
            String body = httpResponse.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);

            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            //原生的url
            String rawUrl = (String) data.get("url");
            //解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            //searchResultUrl为空
            if (StrUtil.isBlank(searchResultUrl)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未返回有效的结果地址");

            }
            return searchResultUrl;
        } catch (BusinessException e) {
            log.error("调用百度以图搜图失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
        }

    }

    public static void main(String[] args) {
        String imageUrl = "https://cdn.pixabay.com/photo/2016/09/29/19/55/doctor-1703644_640.jpg";
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        System.out.println("搜索传给成功，结果： " + imagePageUrl);
    }
}
