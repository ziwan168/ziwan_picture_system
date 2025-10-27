package com.ziwan.ziwanpicturebackend.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ziwan.ziwanpicturebackend.manager.websocket.disruptor.PictureEditEventProducer;
import com.ziwan.ziwanpicturebackend.manager.websocket.model.PictureEditActionEnum;
import com.ziwan.ziwanpicturebackend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.ziwan.ziwanpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.ziwan.ziwanpicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.ziwan.ziwanpicturebackend.model.entity.User;
import com.ziwan.ziwanpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author &#064;date  2025/1/22
 * &#064;description
 */
@Slf4j
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    /**
     * 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
     */
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    /**
     * 保存所有连接的会话，key: pictureId, value: 用户会话集合
     */
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();


    @Resource
    private UserService userService;

    @Resource
    private PictureEditEventProducer pictureEditEventProducer;


    /**
     * 连接建立成功时调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 保存会话到图片的编辑用户集合中
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User user = (User) session.getAttributes().get("user");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        //改造响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 进入编辑状态", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        // 广播给所有图片的编辑用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 接收到前端消息时调用，根据消息类型进行不同的处理
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        // 获取信息
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User user = (User) session.getAttributes().get("user");
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);


    }


    /**
     * 处理进入编辑状态
     *
     * @param pictureEditRequestMessage 请求消息
     * @param session                   会话
     * @param user                      用户
     * @param pictureId                 图片 ID
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {

        if (!pictureEditingUsers.containsKey(pictureId)) {
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户 %s 进入编辑状态", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 广播给所有图片的编辑用户
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }


    /**
     * 处理执行编辑操作
     *
     * @param pictureEditRequestMessage 请求消息
     * @param session                   会话
     * @param user                      用户
     * @param pictureId                 图片 ID
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);

        if (pictureEditActionEnum == null) {
            log.error("未知操作错误");
            return;
        }
        // 判断当前用户是否是正在编辑的用户
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            pictureEditResponseMessage.setEditAction(editAction);
            String message = String.format("用户 %s 执行 %s 操作", user.getUserName(), pictureEditActionEnum.getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage,session);
        }


    }

    /**
     * 处理退出编辑状态
     *
     * @param pictureEditRequestMessage 请求消息
     * @param session                   会话
     * @param user                      用户
     * @param pictureId                 图片 ID
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) throws IOException {

        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            pictureEditingUsers.remove(pictureId);
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("用户 %s 退出编辑状态", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }


    /**
     * 连接关闭时调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        // 获取会话
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User user = (User) session.getAttributes().get("user");
        handleExitEditMessage(null, session, user, pictureId);
        // 移除会话
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(webSocketSessions)) {
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()){
                pictureSessions.remove(pictureId);
            }
        }
        //通知图片的编辑用户
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户 %s 离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }


    /**
     * 广播给所有图片的编辑用户, 除了 excludeSession
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws IOException {
        Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessions)) {
            ObjectMapper objectMapper = getObjectMapper();
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessions) {
                if (session.equals(excludeSession))
                    continue;
                session.sendMessage(textMessage);
            }

        }
    }

    @NotNull
    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
        return objectMapper;
    }

    /**
     * 广播给所有图片的编辑用户
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws IOException {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }
}
