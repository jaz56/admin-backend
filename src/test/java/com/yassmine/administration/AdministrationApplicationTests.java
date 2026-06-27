package com.yassmine.administration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AdministrationApplicationTests {

    @Test
    void contextLoads() {
        // vérifie que le contexte Spring se charge
    }
}