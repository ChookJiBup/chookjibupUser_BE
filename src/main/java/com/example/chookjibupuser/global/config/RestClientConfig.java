package com.example.chookjibupuser.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 카카오 인증/사용자 정보 API 호출용 RestClient를 구성한다.
 * (축제 데이터는 외부 호출 없이 로컬 Postgres에서 JPA로 직접 조회한다.)
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient kakaoRestClient() {
        return RestClient.builder().build();
    }
}
