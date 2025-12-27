package io.realworld.backend.infrastructure.security;

import io.realworld.backend.domain.aggregate.user.User;
import io.realworld.backend.domain.aggregate.user.UserRepository;
import io.realworld.backend.domain.service.AuthenticationService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpringAuthenticationService implements AuthenticationService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /** {@inheritDoc} */
  @Override
  public Optional<User> getCurrentUser() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return Optional.empty();
    }
    if (authentication.getPrincipal().equals("anonymousUser")) {
      return Optional.empty();
    }
    final var ud = (UserDetails) authentication.getPrincipal();
    return userRepository.findByEmail(ud.getUsername());
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> getCurrentToken() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    return Optional.ofNullable((String) authentication.getCredentials());
  }

  /** {@inheritDoc} */
  @Override
  public Optional<User> authenticate(String email, String password) {
    return userRepository
        .findByEmail(email)
        .flatMap(
            u -> {
              if (passwordEncoder.matches(password, u.getPasswordHash())) {
                return Optional.of(u);
              } else {
                return Optional.empty();
              }
            });
  }

  /** {@inheritDoc} */
  @Override
  public String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }
}
