package com.ziwan.ziwanpicturebackend.api.imagesearch;

import com.ziwan.ziwanpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.ziwan.ziwanpicturebackend.api.imagesearch.sub.GetImageAllUrlApi;
import com.ziwan.ziwanpicturebackend.api.imagesearch.sub.GetImagePageBodyApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String body = GetImagePageBodyApi.getImagePageUrl(imageUrl);
        return GetImageAllUrlApi.getImageAllUrl(body);
    }

    public static void main(String[] args) {
        List<ImageSearchResult> imageList = searchImage("https://www.codefather.cn/logo.png");
        imageList.forEach(imageSearchResult -> System.out.println(imageSearchResult.getThumbUrl()));
    }
}

