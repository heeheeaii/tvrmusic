package com.treevalue.atsor;

import com.treevalue.atsor.alg.Alg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author hee
 */

@SpringBootApplication
public class TVRAtsorApplication {
    private static final Logger logger = LoggerFactory.getLogger(TVRAtsorApplication.class);

    private final Alg alg;

    public TVRAtsorApplication(Alg alg) {this.alg = alg;}

    @Bean
    CommandLineRunner runner() {
        return args -> logger.info("setup successfully");
    }

    public static void main(String[] args) {
        SpringApplication.run(TVRAtsorApplication.class, args);
    }
}
