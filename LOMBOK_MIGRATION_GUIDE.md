# Lombok Migration Guide - RealWorld Backend Spring DDD

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Setup](#setup)
4. [Migration Patterns](#migration-patterns)
5. [Phase-by-Phase Migration Plan](#phase-by-phase-migration-plan)
6. [Testing & Verification](#testing--verification)
7. [Common Pitfalls](#common-pitfalls)
8. [Rollback Strategy](#rollback-strategy)

---

## Overview

**Project Status:** 33 Java classes, 0% Lombok adoption
**Estimated Savings:** 500+ lines of boilerplate code (~30% reduction)
**Risk Level:** Low (incremental migration, no runtime changes)

This guide provides a systematic approach to migrating from manual boilerplate to Lombok annotations while preserving all functionality.

---

## Prerequisites

### 1. IDE Setup

**IntelliJ IDEA:**
```
1. Install Lombok Plugin: File → Settings → Plugins → Search "Lombok" → Install
2. Enable Annotation Processing:
   Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   ✓ Enable annotation processing
3. Restart IDE
```

**Eclipse:**
```
1. Download lombok.jar from https://projectlombok.org/download
2. Run: java -jar lombok.jar
3. Select Eclipse installation directory
4. Click "Install/Update"
5. Restart Eclipse
```

**VS Code:**
```
1. Install extension: "Lombok Annotations Support for VS Code"
2. Reload window
```

### 2. Verify Java Compatibility
```bash
# Ensure you're using Java 21 (project requirement)
java -version
# Should show: openjdk version "21.x.x"
```

---

## Setup

### Step 1: Add Lombok Dependency

Edit `pom.xml` and add the dependency in the `<dependencies>` section:

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```

**Important:** Use `<scope>provided</scope>` because Lombok is compile-time only.

### Step 2: Verify Build Configuration

Ensure the annotation processor is configured in `pom.xml` (should already exist from Checker Framework):

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                    <!-- Keep existing Checker Framework path -->
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Step 3: Test Lombok Installation

Create a temporary test class to verify Lombok works:

```java
package io.realworld.backend;

import lombok.Data;

@Data
public class LombokTest {
    private String field;
}
```

Build the project:
```bash
mvn clean compile
```

If successful, delete `LombokTest.java`.

---

## Migration Patterns

### Pattern 1: JPA Entity with Manual Getters/Setters

**BEFORE (Example: User.java - lines 50-87):**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String email;
    private String username;
    private String passwordHash;
    private String bio;
    private String image;

    protected User() {}

    public User(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    // ... 30+ more lines of getters/setters

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", id)
            .add("email", email)
            // ...
            .toString();
    }
}
```

**AFTER:**
```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String email;
    private String username;
    private String passwordHash;
    private String bio;
    private String image;

    // Custom constructors only if needed
    public User(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Custom getters for Optional handling (if needed)
    public Optional<String> getBio() {
        return Optional.ofNullable(bio);
    }

    public Optional<String> getImage() {
        return Optional.ofNullable(image);
    }
}
```

**Annotations Explained:**
- `@Getter` / `@Setter`: Generate getters/setters for all fields
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: Protected no-args constructor for JPA
- `@AllArgsConstructor`: Constructor with all fields (optional)
- `@ToString`: Replaces MoreObjects.toStringHelper()

**Remove Imports:**
```java
// DELETE:
import com.google.common.base.MoreObjects;
```

---

### Pattern 2: Value Object with equals/hashCode

**BEFORE (Example: ArticleFavouriteId.java - 64 lines):**
```java
@Embeddable
public class ArticleFavouriteId implements Serializable {
    private long userId;
    private long articleId;

    protected ArticleFavouriteId() {}

    public ArticleFavouriteId(long userId, long articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getArticleId() { return articleId; }
    public void setArticleId(long articleId) { this.articleId = articleId; }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleFavouriteId that = (ArticleFavouriteId) o;
        return userId == that.userId && articleId == that.articleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, articleId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("userId", userId)
            .add("articleId", articleId)
            .toString();
    }
}
```

**AFTER:**
```java
@Embeddable
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ArticleFavouriteId implements Serializable {
    private long userId;
    private long articleId;
}
```

**Annotations Explained:**
- `@Data`: Generates getters, setters, equals, hashCode, toString
- Reduces 64 lines to ~10 lines (85% reduction)

**Alternative (Immutable Value Object):**
```java
@Embeddable
@Value
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ArticleFavouriteId implements Serializable {
    long userId;
    long articleId;
}
```

**@Value Explained:**
- Makes all fields `private final`
- Generates only getters (no setters)
- Includes equals, hashCode, toString
- Makes class `final`
- Use `force = true` for JPA compatibility (initializes final fields to default values)

---

### Pattern 3: Service with Constructor Injection

**BEFORE (Example: UserService.java - lines 35-44):**
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @Autowired
    public UserService(
            UserRepository userRepository,
            JwtService jwtService,
            AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    // ... methods
}
```

**AFTER:**
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    // ... methods
}
```

**Annotations Explained:**
- `@RequiredArgsConstructor`: Generates constructor for all `final` fields
- Spring auto-detects single constructor, so `@Autowired` is unnecessary
- Reduces 10 lines to 1 annotation

**Note:** If you need `@Autowired` explicitly:
```java
@RequiredArgsConstructor(onConstructor_ = @Autowired)
```

---

### Pattern 4: Entity with Business Logic in Setters

**BEFORE (Example: Article.java - setTitle method):**
```java
@Entity
public class Article {
    private String title;
    private String slug;

    public void setTitle(String title) {
        this.slug = title.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .trim();
        this.title = title;
    }

    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    // ... other getters/setters
}
```

**AFTER:**
```java
@Entity
@Getter
@Setter
public class Article {
    private String title;

    @Setter(AccessLevel.NONE)  // Don't generate setter for slug
    private String slug;

    // Custom setter with business logic
    public void setTitle(String title) {
        this.slug = title.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .trim();
        this.title = title;
    }

    // Other fields will have auto-generated getters/setters
}
```

**Annotations Explained:**
- `@Setter(AccessLevel.NONE)`: Prevents Lombok from generating setter for `slug`
- Class-level `@Getter/@Setter` applies to all fields except those with field-level overrides
- Custom setters override generated ones

---

### Pattern 5: Inner DTO Classes

**BEFORE (Example: Mappers.java - inner class):**
```java
public static class FavouriteInfo {
    private final long userId;
    private final long articleId;

    public FavouriteInfo(long userId, long articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }

    public long getUserId() { return userId; }
    public long getArticleId() { return articleId; }
}
```

**AFTER:**
```java
@Value
public static class FavouriteInfo {
    long userId;
    long articleId;
}
```

**Annotations Explained:**
- `@Value`: Perfect for immutable DTOs
- Generates constructor, getters, equals, hashCode, toString
- Makes class and fields final

---

## Phase-by-Phase Migration Plan

### Phase 1: Value Objects (Lowest Risk, Highest Impact)

**Target:** 85% code reduction in simplest classes

#### Step 1.1: Migrate ArticleFavouriteId
```java
File: src/main/java/io/realworld/backend/domain/aggregate/favourite/ArticleFavouriteId.java
```

1. Add imports:
```java
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
```

2. Add annotations before class:
```java
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
```

3. Delete:
   - All getter/setter methods (lines 24-39)
   - `equals()` method (lines 41-50)
   - `hashCode()` method (lines 52-55)
   - `toString()` method (lines 57-62)

4. Remove imports:
```java
// DELETE:
import com.google.common.base.MoreObjects;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Objects;
```

5. Build and test:
```bash
mvn clean test
```

#### Step 1.2: Migrate FollowRelationId

Apply the same pattern as Step 1.1 to:
```
File: src/main/java/io/realworld/backend/domain/aggregate/follow/FollowRelationId.java
```

#### Step 1.3: Migrate ArticleFavourite

```java
File: src/main/java/io/realworld/backend/domain/aggregate/favourite/ArticleFavourite.java
```

Add annotations:
```java
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
```

Delete all getters/setters and toString().

#### Step 1.4: Migrate FollowRelation

Apply same pattern to:
```
File: src/main/java/io/realworld/backend/domain/aggregate/follow/FollowRelation.java
```

**Verification:**
```bash
mvn clean verify
# Should pass all tests
```

---

### Phase 2: Domain Entities (High Impact)

**Target:** 70-75% code reduction in core entities

#### Step 2.1: Migrate User Entity

```java
File: src/main/java/io/realworld/backend/domain/aggregate/user/User.java
```

1. Add imports:
```java
import lombok.*;
```

2. Add annotations:
```java
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
```

3. Delete lines 50-87 (all getters/setters)

4. Keep custom getters for Optional handling:
```java
// KEEP THESE:
public Optional<String> getBio() {
    return Optional.ofNullable(bio);
}

public Optional<String> getImage() {
    return Optional.ofNullable(image);
}
```

5. Delete toString() method that uses MoreObjects

6. If you want to keep the 3-parameter constructor:
```java
// KEEP:
public User(String email, String username, String passwordHash) {
    this.email = email;
    this.username = username;
    this.passwordHash = passwordHash;
}
```

#### Step 2.2: Migrate Comment Entity

```java
File: src/main/java/io/realworld/backend/domain/aggregate/comment/Comment.java
```

Apply same pattern as User (simpler - no custom getters needed).

#### Step 2.3: Migrate Article Entity

```java
File: src/main/java/io/realworld/backend/domain/aggregate/article/Article.java
```

**Special consideration:** Article has business logic in setTitle()

1. Add annotations:
```java
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
```

2. Mark slug field:
```java
@Setter(AccessLevel.NONE)  // Prevent auto-generated setter
private String slug;
```

3. Keep custom setTitle() method (lines contain slug generation logic)

4. Delete all other getters/setters

5. Delete toString() method

**Verification:**
```bash
mvn clean test
# Verify all JPA tests pass
```

---

### Phase 3: Service Layer (Medium Impact)

**Target:** 30% code reduction, cleaner dependency injection

#### Step 3.1: Migrate UserService

```java
File: src/main/java/io/realworld/backend/application/service/UserService.java
```

1. Add import:
```java
import lombok.RequiredArgsConstructor;
```

2. Add annotation before class:
```java
@RequiredArgsConstructor
```

3. Delete constructor (lines 35-44):
```java
// DELETE:
@Autowired
public UserService(...) {
    this.userRepository = userRepository;
    // ...
}
```

4. Keep all final fields

**Apply same pattern to:**
- `ArticleService.java`
- `ProfileService.java`
- `SpringAuthenticationService.java`
- `JJwtService.java`
- `JwtTokenFilter.java`

**Verification:**
```bash
mvn spring-boot:run
# Verify application starts without errors
```

---

### Phase 4: DTO & Inner Classes (Low Impact)

#### Step 4.1: Migrate Mappers Inner Classes

```java
File: src/main/java/io/realworld/backend/application/dto/Mappers.java
```

For each inner class (FavouriteInfo, MultipleFavouriteInfo):

1. Replace with:
```java
@Value
public static class FavouriteInfo {
    long userId;
    long articleId;
}
```

2. Delete all getters and constructors

#### Step 4.2: Migrate Repository Inner Classes

```java
File: src/main/java/io/realworld/backend/domain/aggregate/favourite/ArticleFavouriteRepository.java
```

Apply `@Value` to `FavouriteCount` interface projection if converted to class.

---

### Phase 5: Optional Enhancements

#### Optional 5.1: Add @Builder to Entities

If you want fluent object creation:

```java
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class User {
    // fields
}
```

Usage:
```java
User user = User.builder()
    .email("test@example.com")
    .username("johndoe")
    .passwordHash("hashed")
    .build();
```

#### Optional 5.2: Convert to Immutable Value Objects

For `ArticleFavouriteId` and `FollowRelationId`, consider true immutability:

```java
@Embeddable
@Value
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ArticleFavouriteId implements Serializable {
    long userId;
    long articleId;
}
```

**Trade-off:** Requires `force = true` for JPA compatibility.

---

## Testing & Verification

### After Each Phase:

#### 1. Compilation Check
```bash
mvn clean compile
```

Should complete without errors.

#### 2. Unit Tests
```bash
mvn test
```

All tests should pass with same results as before.

#### 3. Integration Tests
```bash
mvn verify
```

Should pass all integration tests.

#### 4. IDE Verification

Open each migrated file in your IDE:
- Getters/setters should be visible in structure view
- No red underlines on usages
- Code completion works for generated methods

#### 5. Runtime Test
```bash
mvn spring-boot:run
```

Application should start normally and function identically.

### Full Regression Test Suite

After completing all phases:

```bash
# Clean build
mvn clean

# Full test suite
mvn verify

# Check code coverage (if configured)
mvn jacoco:report

# Run PMD/Checkstyle
mvn pmd:check checkstyle:check
```

### Manual Verification Checklist

- [ ] All entities saved/loaded from database correctly
- [ ] Serialization works (check ArticleFavouriteId)
- [ ] toString() output is reasonable (may differ from MoreObjects)
- [ ] equals/hashCode work in collections (Set, Map)
- [ ] Spring dependency injection works in all services
- [ ] No NullPointerExceptions from generated code
- [ ] API responses unchanged (DTOs serialize correctly)

---

## Common Pitfalls

### Pitfall 1: Forgetting JPA No-Args Constructor Access Level

**Wrong:**
```java
@NoArgsConstructor  // Creates public constructor
```

**Correct:**
```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

JPA entities should have protected/package-private no-args constructors.

---

### Pitfall 2: Losing Custom Business Logic

**Wrong:**
```java
@Setter  // Overwrites custom setTitle()
public class Article {
    private String title;

    public void setTitle(String title) {
        // Custom logic lost!
    }
}
```

**Correct:**
```java
@Getter
@Setter
public class Article {
    private String title;

    @Setter(AccessLevel.NONE)
    private String slug;

    // Custom setter preserved
    public void setTitle(String title) {
        this.slug = generateSlug(title);
        this.title = title;
    }
}
```

---

### Pitfall 3: @Data on JPA Entities

**Warning:**
```java
@Data  // Generates equals/hashCode on all fields including @Id
```

For JPA entities with generated IDs, `@Data` may cause issues:
- equals/hashCode include `id` field
- Transient entities (id = 0) may have incorrect equality

**Better:**
```java
@Getter
@Setter
@ToString
@NoArgsConstructor
// Don't use @EqualsAndHashCode with JPA entities
```

Or explicitly exclude ID:
```java
@EqualsAndHashCode(exclude = "id")
```

---

### Pitfall 4: Immutable @Value with JPA

**Wrong:**
```java
@Value  // Creates final fields
public class User {
    long id;  // JPA can't set final fields!
}
```

**Correct:**
```java
@Value
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
```

The `force = true` initializes final fields to defaults, allowing JPA to use reflection.

---

### Pitfall 5: Checker Framework Conflicts

Your project uses `@Nullable` annotations:

```java
public boolean equals(@Nullable Object o) {
    // ...
}
```

Lombok-generated equals() won't include `@Nullable`. Solutions:

**Option 1:** Accept generated signature (recommended)
```java
// Lombok generates:
public boolean equals(Object o)
```

**Option 2:** Configure Lombok to add annotations
```java
// lombok.config file
lombok.addNullAnnotations = checkerframework
```

---

### Pitfall 6: toString() Changes

Lombok's default toString():
```java
User(id=1, email=test@test.com, username=john, ...)
```

MoreObjects toString():
```java
User{id=1, email=test@test.com, username=john, ...}
```

If your tests assert exact toString() output, update them.

**Custom toString format:**
```java
@ToString(includeFieldNames = true, of = {"id", "username"})
```

---

### Pitfall 7: Lombok Not Processed First

If you get compilation errors about missing getters:

**Solution:** Ensure Lombok processes before Checker Framework in `pom.xml`:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
    </path>
    <path>
        <groupId>org.checkerframework</groupId>
        <artifactId>checker</artifactId>
        <version>...</version>
    </path>
</annotationProcessorPaths>
```

Order matters - Lombok must be first.

---

## Rollback Strategy

### If Something Goes Wrong

#### Rollback Single File

Using Git:
```bash
# Restore specific file
git checkout -- src/main/java/path/to/File.java

# Rebuild
mvn clean compile
```

#### Rollback Entire Phase

```bash
# Check git status
git status

# Restore all modified files
git checkout -- .

# Or reset to last commit
git reset --hard HEAD
```

#### Keep Lombok But Revert Specific Class

Manually restore getters/setters from Git history or:

```bash
# Show previous version
git show HEAD:src/main/java/path/to/File.java > File.java.backup

# Copy back manually
```

### Gradual Rollout

If uncertain, migrate one file at a time:

1. Migrate one value object
2. Test thoroughly
3. Commit: `git commit -m "Migrate ArticleFavouriteId to Lombok"`
4. If issues, rollback just that commit
5. Continue to next file

---

## Best Practices

### 1. Commit After Each Phase

```bash
# After Phase 1
git add .
git commit -m "Phase 1: Migrate value objects to Lombok"
git push

# After Phase 2
git commit -m "Phase 2: Migrate domain entities to Lombok"
git push
```

### 2. Code Review Checklist

For each migrated class, verify:
- [ ] Correct access levels (protected no-args constructor)
- [ ] Custom methods preserved
- [ ] Imports cleaned up (removed MoreObjects, Objects.hash, etc.)
- [ ] Annotations in logical order
- [ ] Tests still pass

### 3. Documentation

Update your project README to mention Lombok:

```markdown
## Development Requirements
- Java 21
- Maven 3.8+
- Lombok plugin for your IDE
```

### 4. Team Communication

Before merging, ensure:
- All team members have Lombok IDE plugin installed
- CI/CD pipeline updated if needed
- Code review guidelines updated

---

## Advanced Configuration (Optional)

### lombok.config File

Create `lombok.config` in project root for custom behavior:

```properties
# Make generated code match project style
lombok.addLombokGeneratedAnnotation = true

# Add null-checking annotations
lombok.addNullAnnotations = checkerframework

# Configure toString format
lombok.toString.includeFieldNames = true

# Don't generate field names in toString
lombok.fieldNameConstants.uppercase = false

# Log field name for @Slf4j
lombok.log.fieldName = logger

# Flagify generated code
lombok.addLombokGeneratedAnnotation = true
```

### Integration with PMD/Checkstyle

Update `.pmdruleset.xml` and `checkstyle.xml` if they complain about:
- Missing comments on fields (Lombok-generated)
- Missing Javadoc on generated methods

Example PMD exclusion:
```xml
<rule ref="category/java/documentation.xml/CommentRequired">
    <properties>
        <property name="methodWithOverrideCommentRequirement" value="Ignored"/>
        <property name="accessorCommentRequirement" value="Ignored"/>
    </properties>
</rule>
```

---

## Summary

### Migration Effort Estimate

| Phase | Files | Estimated Time | Risk |
|-------|-------|---------------|------|
| Setup | 1 (pom.xml) | 15 minutes | Low |
| Phase 1 (Value Objects) | 4 files | 30 minutes | Low |
| Phase 2 (Entities) | 3 files | 45 minutes | Medium |
| Phase 3 (Services) | 6 files | 30 minutes | Low |
| Phase 4 (DTOs) | 2 files | 15 minutes | Low |
| Testing | All | 30 minutes | - |
| **Total** | **16 files** | **~3 hours** | **Low-Medium** |

### Expected Results

- **Before:** 1,800+ lines of code
- **After:** ~1,300 lines of code
- **Reduction:** 500+ lines (28%)
- **Maintainability:** Significantly improved
- **Bugs Introduced:** 0 (if tested properly)

### Success Criteria

- ✓ All tests pass
- ✓ Application runs identically
- ✓ No compilation warnings
- ✓ IDE recognizes all generated methods
- ✓ Code review approved
- ✓ Team has IDE plugins installed

---

## Resources

- **Lombok Documentation:** https://projectlombok.org/features/
- **Lombok Setup:** https://projectlombok.org/setup/maven
- **Common Annotations:** https://projectlombok.org/features/all
- **IDE Plugins:** https://projectlombok.org/setup/overview

---

## Support

If you encounter issues:

1. Check [Common Pitfalls](#common-pitfalls) section
2. Review Lombok official docs
3. Verify IDE plugin is enabled
4. Check annotation processor configuration
5. Run `mvn clean compile -X` for detailed error output

**Generated on:** 2025-12-27
**Project:** realworld-backend-spring-ddd
**Lombok Version:** 1.18.30
