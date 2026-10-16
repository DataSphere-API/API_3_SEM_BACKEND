package com.datasphere.fusex;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requer banco Postgres configurado - roda só em CI/CD")
class FusexApplicationTests {

	@Test
	void contextLoads() {
	}

}