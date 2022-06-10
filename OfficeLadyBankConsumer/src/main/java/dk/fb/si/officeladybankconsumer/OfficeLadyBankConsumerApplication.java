package dk.fb.si.officeladybankconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class OfficeLadyBankConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OfficeLadyBankConsumerApplication.class, args);
	}

}
