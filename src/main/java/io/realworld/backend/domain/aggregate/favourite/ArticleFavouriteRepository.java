package io.realworld.backend.domain.aggregate.favourite;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleFavouriteRepository
    extends JpaRepository<ArticleFavourite, ArticleFavouriteId> {

  @Getter
  @AllArgsConstructor
  class FavouriteCount {
    private final long articleId;
    private final long count;
  }

  int countByIdArticleId(long articleId);

  @Query(
      "SELECT new io.realworld.backend.domain.aggregate.favourite.ArticleFavouriteRepository$"
          + "FavouriteCount(f.id.articleId, COUNT(*)) "
          + "FROM ArticleFavourite f WHERE f.id.articleId IN (:articleIds) GROUP BY f.id.articleId")
  List<FavouriteCount> countByIdArticleIds(List<Long> articleIds);

  List<ArticleFavourite> findByIdUserId(long userId);
}
