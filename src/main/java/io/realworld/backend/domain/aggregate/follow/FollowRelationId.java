package io.realworld.backend.domain.aggregate.follow;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class FollowRelationId implements Serializable {
  private long followerId = 0;
  private long followeeId = 0;
}
