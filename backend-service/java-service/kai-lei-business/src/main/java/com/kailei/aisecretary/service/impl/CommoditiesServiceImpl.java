package com.kailei.aisecretary.service.impl;

import com.kailei.aisecretary.service.ICommoditiesService;
import com.kailei.aisecretary.mapper.IOrderMapper;
import com.kailei.aisecretary.mapper.IProductMapper;
import com.kailei.aisecretary.mapper.ILogisticsMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CommoditiesServiceImpl implements ICommoditiesService {

    @Autowired
    private IOrderMapper iOrderMapper;

    @Autowired
    private IProductMapper iProductMapper;

    @Autowired
    private ILogisticsMapper iLogisticsMapper;

}