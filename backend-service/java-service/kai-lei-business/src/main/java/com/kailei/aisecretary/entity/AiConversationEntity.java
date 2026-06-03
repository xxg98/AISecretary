package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

/**
 * 用户与 AI 秘书的会话。
 */
@Data
@TableName("kl_ai_conversation")
@AutoTable(value = "kl_ai_conversation", comment = "AI秘书会话表")
public class AiConversationEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("用户ID")
    private Long userId;

    @ColumnComment("AI秘书ID")
    private Long secretaryId;

    @ColumnComment("会话标题")
    private String title;

    @ColumnComment("会话类型：user_secretary用户与秘书 secretary_secretary秘书间通信")
    private String conversationType;

    @ColumnComment("状态：0关闭 1进行中")
    private Integer status;
}
