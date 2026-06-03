package com.kailei.aisecretary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kailei.aisecretary.entity.BaseEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IBaseMapper extends BaseMapper<BaseEntity> {
}