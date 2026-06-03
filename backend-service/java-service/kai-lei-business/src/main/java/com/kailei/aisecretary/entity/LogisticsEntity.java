package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

@Data
@TableName("kl_logistics")
@AutoTable(value = "kl_logistics", comment = "物流表")
public class LogisticsEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("订单ID")
    private Long orderId;

    @ColumnComment("物流单号")
    private String logisticsNo;

    @ColumnComment("物流状态：0待发货 1运输中 2签收")
    private Integer logisticsStatus;
}
