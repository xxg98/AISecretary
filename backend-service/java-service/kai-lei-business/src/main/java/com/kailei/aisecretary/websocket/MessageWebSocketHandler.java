package com.kailei.aisecretary.websocket;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {
    private static final String DEFAULT_AVATAR = "https://ww2.sinaimg.cn/mw690/a007f1e0ly1htkqwy4h66j20a00a0aag.jpg";

    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionManager.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        String token = sessionManager.getToken(session);
        String nickname = sessionManager.getNickname(session);
        String content = textMessage.getPayload();
        log.info("WebSocket received, token={}, nickname={}, content={}", token, nickname, content);

        String payloadForSender = buildMessageJson(true, nickname, content);
        String payloadForOthers = buildMessageJson(false, nickname, content);
        sessionManager.broadcast(token, payloadForSender, payloadForOthers);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("WebSocket transport error, sessionId={}", session.getId(), exception);
        sessionManager.remove(session, CloseStatus.SERVER_ERROR);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.remove(session, status);
    }

    private String buildMessageJson(boolean isSelf, String nickname, String content) {
        Message message = new Message();
        message.setIsSelf(isSelf);
        message.setAvatar(DEFAULT_AVATAR);
        message.setNickname(nickname);
        message.setContent(content);
        message.setType("text");
        message.setTimestamp(System.currentTimeMillis());
        return JSONUtil.toJsonStr(message);
    }
}
