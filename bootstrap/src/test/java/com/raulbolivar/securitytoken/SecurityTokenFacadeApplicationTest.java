package com.raulbolivar.securitytoken;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class SecurityTokenFacadeApplicationTest {

    @Test
    void constructorShouldBeCovered() {
        new SecurityTokenFacadeApplication();
    }

    @Test
    void mainShouldDelegateToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            SecurityTokenFacadeApplication.main(new String[]{"--spring.profiles.active=test"});
            mocked.verify(() -> SpringApplication.run(SecurityTokenFacadeApplication.class,
                    new String[]{"--spring.profiles.active=test"}));
        }
    }
}
