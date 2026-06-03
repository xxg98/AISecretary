package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

/**
 * AI 秘书实例，每个用户可以拥有一个或多个 AI 秘书。
 */
@Data
@TableName("kl_ai_secretary")
@AutoTable(value = "kl_ai_secretary", comment = "AI秘书表")
public class AiSecretaryEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("所属用户ID")
    private Long userId;

    @ColumnComment("秘书名称")
    private String secretaryName;

    @ColumnComment("秘书头像地址")
    private String avatarUrl;

    @ColumnComment("秘书定位/角色设定")
    private String rolePrompt;

    @ColumnComment("大模型供应商，如 openai/deepseek/qwen")
    private String modelProvider;

    @ColumnComment("大模型名称")
    private String modelName;

    @ColumnComment("是否默认秘书：0否 1是")
    private Integer defaultFlag;

    @ColumnComment("状态：0禁用 1启用")
    private Integer status;
}
