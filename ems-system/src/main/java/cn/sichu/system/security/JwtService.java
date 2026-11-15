package cn.sichu.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author sichu huang
 * @since 2026/11/15
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long ttlMinutes;

    public JwtService(@Value("${ems.jwt.secret}") String secret,
        @Value("${ems.jwt.issuer:ems}") String issuer,
        @Value("${ems.jwt.access-token-ttl-minutes:480}") long ttlMinutes) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("ems.jwt.secret 长度必须不少于 32 字节");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    public String createToken(Long userId, String username, String roleCode) {
        Instant now = Instant.now();
        return Jwts.builder().issuer(issuer).subject(username).id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(ttlMinutes * 60)))
            .claim("uid", userId).claim("role", roleCode).signWith(key, Jwts.SIG.HS256).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(issuer).build().parseSignedClaims(token)
            .getPayload();
    }
}
