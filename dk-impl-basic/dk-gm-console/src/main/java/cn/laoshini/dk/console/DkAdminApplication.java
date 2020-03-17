package cn.laoshini.dk.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("cn.laoshini.dk")
public class DkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DkAdminApplication.class, args);

    }

}
