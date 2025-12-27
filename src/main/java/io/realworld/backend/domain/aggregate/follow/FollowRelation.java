package io.realworld.backend.domain.aggregate.follow;

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
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@DefaultQualifier(value = Nullable.class, locations = TypeUseLocation.FIELD)
public class FollowRelation {
  @EmbeddedId @NonNull private FollowRelationId id = new FollowRelationId(0, 0);

  public FollowRelation(long followerId, long followeeId) {
    this.id = new FollowRelationId(followerId, followeeId);
  }
}
