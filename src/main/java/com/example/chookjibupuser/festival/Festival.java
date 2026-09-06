// festival/Festival.java (전체)
package com.example.chookjibupuser.festival;

import com.example.chookjibupuser.festival.dto.FestivalPublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 축제 기본 정보. {@code festivals} 테이블은 두 시스템이 같이 쓴다 —
 * 파이썬 파이프라인(공공데이터 API 적재)과 관리자 백엔드(chookjibupAdmin_BE, 수동 등록)가
 * 서로 다른 컬럼 집합을 각자 채운다. 이 엔티티는 두 집합을 합쳐서(union) 매핑하고,
 * 어느 쪽으로 만들어진 축제인지는 신경 쓰지 않는다 — 값이 없으면 그냥 null이다.
 */
@Entity
@Getter
@Table(name = "festivals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Festival {

    @Id
    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "public_id")
    private UUID publicId;

    // ── 관리자 백엔드 쪽 컬럼 ──
    @Column(name = "series_id")
    private Long seriesId;

    @Column(name = "festival_year")
    private Integer year;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "operation_start_time")
    private LocalTime operationStartTime;

    @Column(name = "operation_end_time")
    private LocalTime operationEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "publication_status", length = 30)
    private FestivalPublicationStatus publicationStatus;

    // ── 파이프라인 쪽 컬럼 ──
    @Column(name = "event_place")
    private String eventPlace;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "homepage_url")
    private String homepageUrl;

    // ── 공통 컬럼 ──
    @Column(name = "festival_name")
    private String festivalName;

    @Column(name = "content")
    private String content;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // 지도(HOME02) 마커 표시용. 파이프라인이 공공데이터 API에서 받아 채운다 —
    // 관리자가 수동 등록한 축제는 대부분 null일 수 있다.
    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;
}