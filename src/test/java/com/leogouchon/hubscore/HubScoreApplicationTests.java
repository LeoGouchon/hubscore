package com.leogouchon.hubscore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.config.location=classpath:/application-test.yml",
})
@ActiveProfiles("test")
class HubscoreApplicationTests {

    @Test
    void contextLoads() {
    }

}
