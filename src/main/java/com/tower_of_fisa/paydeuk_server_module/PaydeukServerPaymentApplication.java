package com.tower_of_fisa.paydeuk_server_module;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PaydeukServerPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaydeukServerPaymentApplication.class, args);
	}

}
