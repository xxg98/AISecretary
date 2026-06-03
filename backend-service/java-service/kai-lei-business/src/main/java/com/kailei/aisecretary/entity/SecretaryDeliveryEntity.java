package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

/**
 * 秘书间投递记录，记录一个秘书把文件、任务或消息转交给另一个秘书的过程。
 */
@Data
@TableName("kl_secretary_delivery")
@AutoTable(value = "kl_secretary_delivery", comment = "秘书间投递记录表")
public class SecretaryDeliveryEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("发起用户ID")
    private Long senderUserId;

    @ColumnComment("发起秘书ID")
    private Long senderSecretaryId;

    @ColumnComment("接收用户ID")
    private Long receiverUserId;

    @ColumnComment("接收秘书ID")
    private Long receiverSecretaryId;

    @ColumnComment("投递类型：message/file/todo")
    private String deliveryType;

    @ColumnComment("投递内容摘要")
    private String summary;

    @ColumnComment("关联消息ID")
    private Long messageId;

    @ColumnComment("关联文件ID")
    private Long fileId;

    @ColumnComment("关联待办ID")
    private Long todoId;

    @ColumnComment("状态：0待投递 1已投递 2已接收 3失败")
    private Integer status;

    @ColumnComment("失败原因")
    private String failReason;
}
