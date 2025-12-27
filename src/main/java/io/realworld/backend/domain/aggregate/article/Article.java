package io.realworld.backend.domain.aggregate.article;

import com.google.common.collect.ImmutableSet;
import io.realworld.backend.domain.aggregate.user.User;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;

@Entity
@Getter
@Setter
@ToString
@DefaultQualifier(value = Nullable.class, locations = TypeUseLocation.FIELD)
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id = 0;

  @Setter(AccessLevel.NONE)
  private @NotNull String slug = "";

  private @NotNull String title = "";
  private @NotNull String description = "";
  private @NotNull String body = "";

  @ElementCollection(fetch = FetchType.EAGER)
  private @NotNull Set<String> tags = ImmutableSet.of();

  @ManyToOne private @NotNull User author = new User("", "", "");
  private @NotNull Instant createdAt = Instant.now();
  private @NotNull Instant updatedAt = Instant.now();

  /** Sets title and generate a slug. */
  public void setTitle(String title) {
    this.slug =
        title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\’|\\”|\\s\\?\\,\\.]+", "-")
            + "-"
            + ThreadLocalRandom.current().nextInt();
    this.title = title;
  }

  public void setTags(Set<String> tags) {
    this.tags = ImmutableSet.copyOf(tags);
  }

  @PreUpdate
  public void onUpdate() {
    updatedAt = Instant.now();
  }
}
