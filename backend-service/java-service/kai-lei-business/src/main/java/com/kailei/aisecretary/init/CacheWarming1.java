package com.kailei.aisecretary.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kailei.aisecretary.entity.ProductEntity;
import com.kailei.aisecretary.mapper.IProductMapper;
import com.kailei.aisecretary.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 项目完全启动后执行
 */
@Slf4j
@Component
public class CacheWarming1 implements ApplicationRunner {

    @Autowired
    private IProductMapper iProductMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("------------------------ApplicationRunner-------------------------");

        //1、查询数据库 商品表
        List<ProductEntity> productEntities = iProductMapper.selectList(new LambdaQueryWrapper<>());
        //2、把商品存入到redis中 iter
        for (ProductEntity productEntity : productEntities) {
            String key = "ddz:product:"+productEntity.getId();
            redisUtil.set(key,productEntity);
        }

    }

}
