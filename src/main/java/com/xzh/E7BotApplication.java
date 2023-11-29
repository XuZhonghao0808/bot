package com.xzh;

import love.forte.simboot.spring.autoconfigure.EnableSimbot;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//@EnableScheduling //开启定时任务
@MapperScan("com.xzh.mapper")
@SpringBootApplication
@EnableSimbot
public class E7BotApplication {

	private static final Logger log = LoggerFactory.getLogger(E7BotApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(E7BotApplication.class, args);
	}

}
