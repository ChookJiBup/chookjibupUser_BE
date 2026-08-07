package com.example.chookjibupuser.auth.command.infrastructure;

import com.example.chookjibupuser.auth.support.UserPrincipal;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.user.UserAccount;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 사용자 Access Token을 HMAC-SHA256 JWT 형식으로 발급하고 검증한다.
 * (별도 JWT 라이브러리 없이 직접 서명한다 — demoAdmin과 동일한 방식.)
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String USER_SUBJECT_TYPE = "USER";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtProperties jwtProperties;

    public String createAccessToken(UserAccount userAccount) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds());

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subjectType", USER_SUBJECT_TYPE);
        payload.put("sub", userAccount.getUserId());
        payload.put("nickname", userAccount.getNickname());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;

        return unsignedToken + "." + sign(unsignedToken);
    }

    public UserPrincipal parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        String unsignedToken = parts[0] + "." + parts[1];
        if (!sign(unsignedToken).equals(parts[2])) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        JsonNode payload = decodePayload(parts[1]);
        validateUserSubject(payload);
        long exp = payload.path("exp").asLong();
        if (Instant.now().getEpochSecond() > exp) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }

        return new UserPrincipal(payload.path("sub").asLong(), payload.path("nickname").asText());
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.accessTokenExpirationSeconds();
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(value);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to encode JWT json.", exception);
        }
    }

    private JsonNode decodePayload(String encodedPayload) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(encodedPayload);
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    private void validateUserSubject(JsonNode payload) {
        JsonNode subjectType = payload.get("subjectType");
        if (subjectType != null && !USER_SUBJECT_TYPE.equals(subjectType.asText())) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            byte[] secret = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign JWT.", exception);
        }
    }
}
