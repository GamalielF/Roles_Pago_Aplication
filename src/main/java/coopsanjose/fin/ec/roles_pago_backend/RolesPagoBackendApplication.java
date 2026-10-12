package coopsanjose.fin.ec.roles_pago_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RolesPagoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RolesPagoBackendApplication.class, args);
	}

}
