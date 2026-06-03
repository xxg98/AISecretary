package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

/**
 * 文件元数据表，用于记录用户发给秘书、秘书转交给其他人的文件。
 */
@Data
@TableName("kl_file")
@AutoTable(value = "kl_file", comment = "文件表")
public class FileEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("上传用户ID")
    private Long uploaderUserId;

    @ColumnComment("文件原始名称")
    private String originalName;

    @ColumnComment("文件存储名称")
    private String storageName;

    @ColumnComment("文件访问地址")
    private String fileUrl;

    @ColumnComment("文件存储路径")
    private String storagePath;

    @ColumnComment("文件类型")
    private String contentType;

    @ColumnComment("文件大小，单位字节")
    private Long fileSize;

    @ColumnComment("文件摘要")
    private String fileHash;

    @ColumnComment("状态：0删除 1正常")
    private Integer status;
}
