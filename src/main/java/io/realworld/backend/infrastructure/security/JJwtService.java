package io.realworld.backend.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.realworld.backend.domain.aggregate.user.User;
import io.realworld.backend.domain.aggregate.user.UserRepository;
import io.realworld.backend.domain.service.JwtService;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JJwtService implements JwtService {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.sessionTime}")
  private int sessionTime;

  private final UserRepository userRepository;

  /** {@inheritDoc} */
  @Override
  public String generateToken(User user) {
    return Jwts.builder()
        .subject(Long.toString(user.getId()))
        .expiration(new Date(System.currentTimeMillis() + sessionTime * 1000))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<User> getUser(String token) {
    try {
      final var subject =
          Jwts.parser()
              .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
              .build()
              .parseSignedClaims(token)
              .getPayload()
              .getSubject();
      final var userId = Long.parseLong(subject);
      return userRepository.findById(userId);
    } catch (io.jsonwebtoken.JwtException | NumberFormatException e) {
      return Optional.empty();
    }
  }
}
