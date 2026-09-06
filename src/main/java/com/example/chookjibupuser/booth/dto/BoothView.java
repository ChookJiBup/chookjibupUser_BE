// booth/dto/BoothView.java (신규)
package com.example.chookjibupuser.booth.dto;

import com.example.chookjibupuser.booth.BoothInfo;

public record BoothView(
        Long boothId,
        String name,
        String content,
        String location
) {

    public static BoothView of(BoothInfo booth) {
        return new BoothView(booth.getId(), booth.getBoothName(), booth.getBoothContent(), booth.getBoothLocation());
    }
}