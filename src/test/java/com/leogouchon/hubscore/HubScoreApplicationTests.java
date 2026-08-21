package com.leogouchon.hubscore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
})
class HubscoreApplicationTests {

    @Test
    void contextLoads() {
    }

}
