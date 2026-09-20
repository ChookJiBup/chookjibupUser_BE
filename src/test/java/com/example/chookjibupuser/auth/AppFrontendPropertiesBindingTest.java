package com.example.chookjibupuser.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.chookjibupuser.auth.command.infrastructure.mail.AppFrontendProperties;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * 운영 서버는 application.yml이 아니라 환경변수로 프론트엔드 주소를 넣는다.
 * 이 값이 바인딩되지 않으면 비밀번호 재설정 요청이 NullPointerException으로 500이 된다.
 */
class AppFrontendPropertiesBindingTest {

    @Test
    @DisplayName("APP_FRONTEND_BASEURL 환경변수가 app.frontend.base-url로 바인딩된다")
    void bindsFromEnvironmentVariable() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new SystemEnvironmentPropertySource(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                Map.of("APP_FRONTEND_BASEURL", "https://user.chookjibup.store")));

        AppFrontendProperties properties = Binder.get(environment)
                .bind("app.frontend", AppFrontendProperties.class)
                .get();

        assertThat(properties.baseUrl()).isEqualTo("https://user.chookjibup.store");
    }
}
