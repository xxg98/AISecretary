package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

import java.util.Date;

/**
 * 待办事项表。对方秘书收到任务、文件或确认请求后，会在接收人的待办中创建记录。
 */
@Data
@TableName("kl_todo")
@AutoTable(value = "kl_todo", comment = "待办事项表")
public class TodoEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("发起用户ID")
    private Long creatorUserId;

    @ColumnComment("发起秘书ID")
    private Long creatorSecretaryId;

    @ColumnComment("接收用户ID")
    private Long receiverUserId;

    @ColumnComment("接收秘书ID")
    private Long receiverSecretaryId;

    @ColumnComment("待办标题")
    private String title;

    @ColumnComment("待办内容")
    private String content;

    @ColumnComment("待办类型：send_file/confirm/review/follow_up/custom")
    private String todoType;

    @ColumnComment("优先级：1低 2中 3高")
    private Integer priority;

    @ColumnComment("截止时间")
    private Date deadlineTime;

    @ColumnComment("状态：0待处理 1处理中 2已完成 3已取消")
    private Integer status;

    @ColumnComment("来源消息ID")
    private Long sourceMessageId;

    @ColumnComment("关联文件ID")
    private Long fileId;
}
