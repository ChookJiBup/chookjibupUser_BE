// roadmap/MapImageProperties.java
package com.example.chookjibupuser.roadmap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** app.map.image-base-url. 비어있으면 mapImageUrl은 null로 내려간다. */
@ConfigurationProperties(prefix = "app.map")
public record MapImageProperties(String imageBaseUrl) {
}