package cn.laoshini.dk.config.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class DkJdbcConfigCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DkJdbcConfigCenterApplication.class, args);
    }

}
