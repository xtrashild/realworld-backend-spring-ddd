package io.realworld.backend.domain.aggregate.comment;

import io.realworld.backend.domain.aggregate.article.Article;
import io.realworld.backend.domain.aggregate.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;

@Entity
@Getter
@Setter
@ToString
@DefaultQualifier(value = Nullable.class, locations = TypeUseLocation.FIELD)
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id = 0;

  @ManyToOne private @NonNull Article article = new Article();
  @ManyToOne private @NonNull User author = new User("", "", "");
  private @NotNull String body = "";
  private @NotNull Instant createdAt = Instant.now();
  private @NotNull Instant updatedAt = Instant.now();
}
