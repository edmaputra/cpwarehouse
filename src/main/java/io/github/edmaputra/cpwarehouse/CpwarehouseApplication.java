package io.github.edmaputra.cpwarehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class CpwarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CpwarehouseApplication.class, args);
    }

}

