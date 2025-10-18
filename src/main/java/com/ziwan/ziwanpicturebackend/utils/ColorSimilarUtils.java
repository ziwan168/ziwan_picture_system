package com.ziwan.ziwanpicturebackend.utils;

import java.awt.*;

public class ColorSimilarUtils {
    private ColorSimilarUtils() {
        //禁止实例化
    }


    /**
     *
     * @param color1 颜色1
     * @param color2 颜色2
     * @return 颜色相似度(0到1之间)
     */
    public static double calculateSimilarity(Color color1, Color color2) {

        int red1 = color1.getRed();
        int green1 = color1.getGreen();
        int blue1 = color1.getBlue();
        int red2 = color2.getRed();
        int green2 = color2.getGreen();
        int blue2 = color2.getBlue();
        //计算颜色相似度:颜色相似度:欧式距离
        double distance = Math.sqrt(Math.pow(red1 - red2, 2) + Math.pow(green1 - green2, 2) + Math.pow(blue1 - blue2, 2));
        return 1 - distance / Math.sqrt(3 * Math.pow(255, 2));
    }

    /**
     * 根据十六进制颜色计算颜色相似度
     *
     * @param hexColor1 颜色1
     * @param hexColor2 颜色2
     * @return 颜色相似度(0到1之间)
     */
    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        Color color1 = Color.decode(hexColor1);
        Color color2 = Color.decode(hexColor2);
        return calculateSimilarity(color1, color2);
    }



}
