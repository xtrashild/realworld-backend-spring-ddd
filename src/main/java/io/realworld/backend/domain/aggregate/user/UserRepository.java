package io.realworld.backend.domain.aggregate.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String username);

  Optional<User> findByUsername(String username);
}
