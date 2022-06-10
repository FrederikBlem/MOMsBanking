package dk.fb.si.mumsbankconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class MumsBankConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MumsBankConsumerApplication.class, args);
    }

}
