package com.kailei.aisecretary.init;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Bean创建好时执行
 */
@Slf4j
@Component
public class CacheWarming {


    @PostConstruct
    public void dataLoading(){
        log.info("====================dataLoading===============");
    }
}
