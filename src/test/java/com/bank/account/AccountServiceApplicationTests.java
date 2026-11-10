package com.bank.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/account-service-db"
})
class AccountServiceApplicationTests {

    @Test
    @DisplayName("Should verify that the account-service context loads smoothly")
    void contextLoads() {
        // Confirms that all non-blocking pipelines, repositories, and configurations align.
    }
}
