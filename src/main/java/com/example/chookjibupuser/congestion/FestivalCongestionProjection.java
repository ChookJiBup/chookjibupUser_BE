package com.example.chookjibupuser.congestion;

import java.time.OffsetDateTime;

public interface FestivalCongestionProjection {

    String getCongestionLevel();

    OffsetDateTime getUpdatedAt();
}
