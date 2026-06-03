package com.kailei.aisecretary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kailei.aisecretary.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IOrderMapper extends BaseMapper<OrderEntity> {
}