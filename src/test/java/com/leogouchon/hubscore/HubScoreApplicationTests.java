package com.leogouchon.hubscore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "jwt.secret=test_secret_key_for_context_loads_please_change_12345",
        "jwt.expirationMs=3600000"
})
class HubScoreApplicationTests {

    @Test
    void contextLoads() {
    }

}
