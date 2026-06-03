package com.kailei.aisecretary.websocket;

import lombok.Data;

@Data
public class Message {
    private Boolean isSelf;
    private String avatar;
    private String nickname;
    private String content;
    private String type;
    private Long timestamp;
}
