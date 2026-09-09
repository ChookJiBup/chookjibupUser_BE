package com.example.chookjibupuser.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 날짜 기반 판단에 쓰는 서비스 기준 시계를 제공한다.
 *
 * <p>축제 시작일/종료일은 한국 날짜로 등록되므로 "오늘"도 한국 날짜여야 한다.
 * 운영 서버(EC2)의 기본 시간대는 UTC라서, 시계를 지정하지 않고 {@code LocalDate.now()}를
 * 쓰면 한국시간 00:00~09:00 사이에 하루 전 날짜가 나온다 — 그 시간대에 오늘 시작한
 * 축제가 방문객 앱에서만 "진행 예정"으로 보였던 원인이다.
 *
 * <p>관리자 백엔드(chookjibupAdmin_BE)도 같은 이유로 Asia/Seoul 시계를 쓴다.
 * 두 시스템이 같은 축제에 같은 진행 상태를 내려주려면 기준 시간대가 같아야 한다.
 */
@Configuration
public class TimeConfig {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(SERVICE_ZONE_ID);
    }
}
