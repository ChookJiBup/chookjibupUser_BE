// roadmap/dto/ZoneView.java
package com.example.chookjibupuser.roadmap.dto;

import java.util.List;
import java.util.UUID;

public record ZoneView(UUID zoneId, String name, int sortOrder, List<NodeView> booths) {
}