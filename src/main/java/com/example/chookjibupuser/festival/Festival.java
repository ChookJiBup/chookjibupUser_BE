package com.example.chookjibupuser.festival;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 축제 데이터. 파이썬 파이프라인(julcut_data_pipeline)이 공공데이터 API로 채우고
 * UPSERT로 계속 갱신하는 {@code festivals} 테이블에 그대로 매핑한다.
 *
 * <p>이 서버는 이 테이블을 읽기만 한다 — 쓰기(등록/수정)는 이 서비스 범위가 아니라서
 * 저장/수정 메서드를 두지 않았다. 테이블에는 이 외에도 pipeline이 매칭한
 * visitor_YYYY_* 동적 컬럼, raw_payload 등이 더 있지만, 목록 화면에 필요한
 * 컬럼만 골라 매핑했다(컴팩트하게 유지 — 필요한 컬럼이 늘면 여기 추가하면 된다).</p>
 */
@Entity
@Getter
@Table(name = "festivals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Festival {

    @Id
    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "festival_name")
    private String festivalName;

    @Column(name = "event_place")
    private String eventPlace;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "content")
    private String content;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "homepage_url")
    private String homepageUrl;
}
