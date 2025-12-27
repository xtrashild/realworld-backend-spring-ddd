package io.realworld.backend.domain.aggregate.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * Domain entity representing an application user.
 *
 * <p>Contains the user's identity (id, email, username) and credentials (passwordHash), as well as
 * optional profile information (bio, image).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@DefaultQualifier(value = Nullable.class, locations = TypeUseLocation.FIELD)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id = 0;

  private @NotNull String email = "";
  private @NotNull String username = "";
  private @NotNull String passwordHash = "";

  private String bio = null;
  private String image = null;

  /** Creates User instance. */
  public User(String email, String username, String passwordHash) {
    this.email = email;
    this.username = username;
    this.passwordHash = passwordHash;
  }

  public Optional<String> getBio() {
    return Optional.ofNullable(bio);
  }

  public Optional<String> getImage() {
    return Optional.ofNullable(image);
  }
}
