package com.kailei.aisecretary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kailei.aisecretary.entity.BannerEntity;
import com.kailei.aisecretary.service.ISlideService;
import com.kailei.aisecretary.mapper.IBannerMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SlideServiceImpl implements ISlideService {

    @Autowired
    private IBannerMapper iBannerMapper;

    @Override
    public List<BannerEntity> getBanners() {
        return iBannerMapper.selectList(new QueryWrapper<>());
    }
}