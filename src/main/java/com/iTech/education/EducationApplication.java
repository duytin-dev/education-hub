package com.iTech.education;

import com.iTech.education.config.PaymentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PaymentProperties.class)
public class EducationApplication {

	public static void main(String[] args) {
		System.out.println();
		SpringApplication.run(EducationApplication.class, args);
	}

}
