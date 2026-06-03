package com.kailei.aisecretary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kailei.aisecretary.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IProductMapper extends BaseMapper<ProductEntity> {
}