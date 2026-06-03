package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

/**
 * AI 秘书消息记录，保留用户消息、AI 回复和秘书间消息。
 */
@Data
@TableName("kl_ai_message")
@AutoTable(value = "kl_ai_message", comment = "AI秘书消息表")
public class AiMessageEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("会话ID")
    private Long conversationId;

    @ColumnComment("发送用户ID，可为空")
    private Long senderUserId;

    @ColumnComment("发送秘书ID，可为空")
    private Long senderSecretaryId;

    @ColumnComment("接收用户ID，可为空")
    private Long receiverUserId;

    @ColumnComment("接收秘书ID，可为空")
    private Long receiverSecretaryId;

    @ColumnComment("消息角色：user/assistant/system/secretary")
    private String messageRole;

    @ColumnComment("消息类型：text/file/task/system")
    private String messageType;

    @ColumnComment("消息内容")
    private String content;

    @ColumnComment("关联文件ID")
    private Long fileId;

    @ColumnComment("关联待办ID")
    private Long todoId;

    @ColumnComment("大模型响应原始内容")
    private String rawResponse;

    @ColumnComment("状态：0失败 1成功")
    private Integer status;
}
