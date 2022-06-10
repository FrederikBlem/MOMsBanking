package dk.fb.si.momsloanproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class MoMsLoanProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoMsLoanProducerApplication.class, args);
    }

}
