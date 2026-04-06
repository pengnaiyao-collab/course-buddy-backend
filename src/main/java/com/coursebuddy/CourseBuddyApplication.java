package com.coursebuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * 课伴应用启动类
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableSpringDataWebSupport
public class CourseBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseBuddyApplication.class, args);
    }
}
