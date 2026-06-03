package com.kailei.aisecretary;

import org.dromara.autotable.springboot.EnableAutoTable;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//激活autotable
@EnableFileStorage
@EnableAutoTable
@SpringBootApplication
public class KaiLeiAiSecretaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaiLeiAiSecretaryApplication.class, args);
    }

}
