package com.example.chookjibupuser.booth.dto;

import com.example.chookjibupuser.booth.BoothQueue;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** booth 도메인의 순수한 뷰. 부스 이름 같은 다른 도메인 정보는 담지 않는다. */
public record BoothQueueView(
        Long boothId,
        BigDecimal tailLatitude,
        BigDecimal tailLongitude,
        Integer queueTailMeters,
        String pathGeometry,
        LocalDateTime updatedAt
) {

    public static BoothQueueView of(BoothQueue queue) {
        return new BoothQueueView(
                queue.getBoothId(),
                queue.getTailLatitude(),
                queue.getTailLongitude(),
                queue.getQueueTailMeters(),
                queue.getPathGeometry(),
                queue.getUpdatedAt()
        );
    }
}