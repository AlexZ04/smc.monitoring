package ru.smc.monitoring;

import ru.smc.monitoring.application.infrastructure.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		DotenvLoader.load();
		SpringApplication.run(Application.class, args);
	}

}
