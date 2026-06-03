package com.kailei.aisecretary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kailei.aisecretary.entity.UsersEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserMapper extends BaseMapper<UsersEntity> {
}
