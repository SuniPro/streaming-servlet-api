package com.taekang.streamingreactiveapi;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class StreamingReactiveApiApplication {

    public static void main(String[] args) {
        log.info("Application started Start Time : {}", LocalDateTime.now());
        SpringApplication.run(StreamingReactiveApiApplication.class, args);
    }
}
