package com.kailei.aisecretary.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager {
    private static final String TOKEN_KEY = "token";
    private static final String NICKNAME_KEY = "nickname";

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void add(WebSocketSession session) {
        String token = getQueryParam(session, TOKEN_KEY).orElse(session.getId());
        String nickname = getQueryParam(session, NICKNAME_KEY).orElse("匿名用户");
        session.getAttributes().put(TOKEN_KEY, token);
        session.getAttributes().put(NICKNAME_KEY, nickname);
        sessions.put(token, session);
        log.info("WebSocket connected, token={}, nickname={}, online={}", token, nickname, sessions.size());
    }

    public void remove(WebSocketSession session, CloseStatus status) {
        String token = getToken(session);
        if (token != null) {
            sessions.remove(token);
        }
        log.info("WebSocket closed, token={}, status={}, online={}", token, status, sessions.size());
    }

    public void sendToUser(String token, String payload) {
        WebSocketSession session = sessions.get(token);
        if (session == null || !session.isOpen()) {
            sessions.remove(token);
            return;
        }
        send(session, payload);
    }

    public void broadcast(String senderToken, String payloadForSender, String payloadForOthers) {
        sessions.forEach((token, session) -> {
            if (!session.isOpen()) {
                sessions.remove(token);
                return;
            }
            send(session, token.equals(senderToken) ? payloadForSender : payloadForOthers);
        });
    }

    public String getToken(WebSocketSession session) {
        Object token = session.getAttributes().get(TOKEN_KEY);
        return token == null ? null : token.toString();
    }

    public String getNickname(WebSocketSession session) {
        Object nickname = session.getAttributes().get(NICKNAME_KEY);
        return nickname == null ? "匿名用户" : nickname.toString();
    }

    public int onlineCount() {
        return sessions.size();
    }

    private void send(WebSocketSession session, String payload) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException e) {
            log.warn("WebSocket send failed, sessionId={}", session.getId(), e);
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException closeException) {
                log.warn("WebSocket close failed, sessionId={}", session.getId(), closeException);
            }
        }
    }

    private Optional<String> getQueryParam(WebSocketSession session, String key) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            return Optional.empty();
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && key.equals(keyValue[0])) {
                return Optional.of(keyValue[1]);
            }
        }
        return Optional.empty();
    }
}
