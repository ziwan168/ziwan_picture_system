package com.ziwan.ziwanpicturebackend.api.aliyunai;

import cn.hutool.http.HttpRequest;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.ziwan.ziwanpicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.ziwan.ziwanpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.ziwan.ziwanpicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.ziwan.ziwanpicturebackend.exception.BusinessException;
import com.ziwan.ziwanpicturebackend.exception.ErrorCode;
import com.ziwan.ziwanpicturebackend.exception.ThrowUtils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ALiYunAiApi {

    @Value("${aliYun.apiKey}")
    private String apiKey;

    //创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    //获取任务地址
    public static final String GET_OUT_PAINTING_TASK_URL = " https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建任务请求
     *
     * @param createOutPaintingTaskRequest 创建任务请求参数
     * @return 创建任务结果
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer" + apiKey)
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));

        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("创建任务失败:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "ai扩图失败");
            }
            CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            if (createOutPaintingTaskResponse.getCode() != null) {
                log.error("创建任务失败:{}", createOutPaintingTaskResponse.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "ai扩图失败");
            }
            return createOutPaintingTaskResponse;

        }


    }

    /**
     * 获取任务结果
     *
     * @param taskId 任务ID
     * @return 任务结果
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAM_ERROR);
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header("Authorization", "Bearer" + apiKey)
                .header("Content-Type", "application/json").execute()) {
            if (!httpResponse.isOk()) {
                log.error("获取任务结果失败:{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "ai扩图失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }

}
