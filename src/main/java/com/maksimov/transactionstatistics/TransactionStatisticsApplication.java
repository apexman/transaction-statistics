package com.maksimov.transactionstatistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransactionStatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionStatisticsApplication.class, args);
    }

    @Bean
    public ObjectMapper getMapper() {
        return new ObjectMapper();
    }
}

