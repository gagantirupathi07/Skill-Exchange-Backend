
package com.SkillExchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SkillExchangeApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                SkillExchangeApplication.class,
                args
        );

        System.out.println("Skill Exchange Application Started");
    }
}
