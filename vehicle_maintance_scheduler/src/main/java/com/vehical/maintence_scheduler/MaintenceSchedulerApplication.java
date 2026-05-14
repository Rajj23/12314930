package com.vehical.maintence_scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.vehical.maintence_scheduler", "com.logger.middle"})
public class MaintenceSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaintenceSchedulerApplication.class, args);
	}

}
