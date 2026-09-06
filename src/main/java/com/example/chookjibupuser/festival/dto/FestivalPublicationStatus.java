package com.example.chookjibupuser.festival.dto;

/**
 * 관리자 백엔드의 FestivalStatus를 그대로 반영. plain VARCHAR라 직접 매핑 가능.
 * 2026-08 기준 DRAFT 하나뿐이라, 아직 이 값으로 노출 필터링은 안 한다
 * (필터링하면 축제가 하나도 안 보임). PUBLISHED 등이 추가되면 그때 필터 추가.
 */
public enum FestivalPublicationStatus {
    DRAFT
}