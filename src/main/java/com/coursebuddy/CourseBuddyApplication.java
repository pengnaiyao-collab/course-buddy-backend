package com.coursebuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CourseBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseBuddyApplication.class, args);
    }
}