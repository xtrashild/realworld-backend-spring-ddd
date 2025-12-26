package io.realworld.backend.domain.aggregate.follow;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRelationRepository extends JpaRepository<FollowRelation, FollowRelationId> {
  List<FollowRelation> findByIdFollowerId(long followerId);
}
