package io.realworld.backend.domain.aggregate.favourite;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class ArticleFavouriteId implements Serializable {
  private long userId;
  private long articleId;
}
