package com.example.chookjibupuser.congestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 부스 혼잡도 이력. {@code booth_congestion} 테이블에 매핑한다.
 *
 * <p>{@code congestion_level} 컬럼은 Postgres 네이티브 ENUM 타입(스키마에
 * {@code CREATE TYPE congestion_level AS ENUM (...)}로 정의됨)이라 JPA 엔티티
 * 필드로 직접 매핑하지 않았다 — JDBC가 이 컬럼을 VARCHAR가 아니라 OTHER/사용자
 * 정의 타입으로 보고하기 때문에, 일반 String 매핑은 {@code ddl-auto: validate}에서
 * 타입 불일치로 실패할 위험이 크다. 대신 {@link BoothCongestionRepository}의
 * 네이티브 쿼리에서 {@code ::text}로 캐스팅해서 안전하게 읽어온다.</p>
 */
@Entity
@Getter
@Table(name = "booth_congestion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoothCongestion {

    @Id
    @Column(name = "congestion_id")
    private Long congestionId;

    @Column(name = "booth_id")
    private Long boothId;

    @Column(name = "wait_minutes")
    private Integer waitMinutes;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
