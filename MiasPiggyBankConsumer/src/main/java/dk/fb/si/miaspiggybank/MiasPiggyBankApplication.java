package dk.fb.si.miaspiggybank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class MiasPiggyBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiasPiggyBankApplication.class, args);
    }

}
