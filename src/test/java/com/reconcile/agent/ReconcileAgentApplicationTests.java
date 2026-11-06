package com.reconcile.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "vultr.inference.api-key=test-key")
class ReconcileAgentApplicationTests {

	@Test
	void contextLoads() {
	}

}
