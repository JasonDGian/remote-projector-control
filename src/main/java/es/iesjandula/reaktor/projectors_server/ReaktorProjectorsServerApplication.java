package es.iesjandula.reaktor.projectors_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;


@SpringBootApplication
@ComponentScan(basePackages = {"es.iesjandula"})
public class ReaktorProjectorsServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReaktorProjectorsServerApplication.class, args);
	}

}
