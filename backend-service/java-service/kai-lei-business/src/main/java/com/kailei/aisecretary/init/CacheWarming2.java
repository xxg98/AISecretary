package com.kailei.aisecretary.init;

import com.kailei.aisecretary.utils.MapperGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 项目完全启动后执行
 */
@Slf4j
@Component
public class CacheWarming2 implements CommandLineRunner {

    @Autowired
    private MapperGeneratorUtil mapperGeneratorUtil;

    @Override
    public void run(String... args) throws Exception {
        log.info("-----------------------CommandLineRunner-----------------------------");
        mapperGeneratorUtil.generateMappers();
    }

}
