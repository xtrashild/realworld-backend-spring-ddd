package io.realworld.backend.domain.aggregate.comment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findByArticleId(long articleId);

  void deleteByArticleId(long articleId);
}
