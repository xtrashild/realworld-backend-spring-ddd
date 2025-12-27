package io.realworld.backend.domain.aggregate.favourite;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@DefaultQualifier(value = Nullable.class, locations = TypeUseLocation.FIELD)
public class ArticleFavourite {
  @EmbeddedId @NonNull private ArticleFavouriteId id = new ArticleFavouriteId(0, 0);

  public ArticleFavourite(long userId, long articleId) {
    this.id = new ArticleFavouriteId(userId, articleId);
  }
}
