package com.example.chookjibupuser.booth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 축제 부스 정보. 파이썬 파이프라인(schema.sql)이 만들어둔 {@code booth_info}
 * 테이블에 매핑한다. 이 서버는 읽기 전용이다 — 부스 등록/수정은 관리자 쪽 책임이다.
 */
@Entity
@Getter
@Table(name = "booth_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoothInfo {

    @Id
    @Column(name = "booth_id")
    private Long boothId;

    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "booth_name")
    private String boothName;

    @Column(name = "booth_content")
    private String boothContent;

    @Column(name = "booth_location")
    private String boothLocation;
}
