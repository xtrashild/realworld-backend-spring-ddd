# RealWorld Backend API and Database Documentation

## REST API Endpoints

### Authentication & User Management

#### Users API (`/api/users`)

| Method | Endpoint | Description | Auth Required | Status Codes |
|--------|----------|-------------|---------------|--------------|
| POST | `/users` | Register a new user | No | 201, 422 |
| POST | `/users/login` | Login for existing user | No | 200, 401, 422 |

#### User API (`/api/user`)

| Method | Endpoint | Description | Auth Required | Status Codes |
|--------|----------|-------------|---------------|--------------|
| GET | `/user` | Get currently logged-in user | Yes | 200, 401, 422 |
| PUT | `/user` | Update current user information | Yes | 200, 401, 422 |

### Profiles

#### Profiles API (`/api/profiles`)

| Method | Endpoint | Description | Auth Required | Status Codes |
|--------|----------|-------------|---------------|--------------|
| GET | `/profiles/{username}` | Get a user profile by username | No | 200, 401, 422 |
| POST | `/profiles/{username}/follow` | Follow a user by username | Yes | 200, 401, 422 |
| DELETE | `/profiles/{username}/follow` | Unfollow a user by username | Yes | 200, 401, 422 |

### Articles

#### Articles API (`/api/articles`)

| Method | Endpoint | Description | Auth Required | Status Codes |
|--------|----------|-------------|---------------|--------------|
| GET | `/articles` | Get recent articles globally with optional filters (tag, author, favorited, limit, offset) | No | 200, 401, 422 |
| POST | `/articles` | Create a new article | Yes | 201, 401, 422 |
| GET | `/articles/feed` | Get recent articles from users you follow | Yes | 200, 401, 422 |
| GET | `/articles/{slug}` | Get a specific article by slug | No | 200, 422 |
| PUT | `/articles/{slug}` | Update an article | Yes | 200, 401, 422 |
| DELETE | `/articles/{slug}` | Delete an article | Yes | 200, 401, 422 |

### Favorites

| Method | Endpoint | Description | Auth Required | Status Codes |
|--------|----------|-------------|---------------|--------------|
| POST | `/articles/{slug}/favorite` | Favorite an article | Yes | 200, 401, 422 |
| DELETE | `/articles/{slug}/favorite` | Unfavorite an article | Yes | 200, 401, 422 |

### Comments

| Method | Endpoint | Description | Auth Required | Status Codes |
|--------|----------|-------------|---------------|--------------|
| GET | `/articles/{slug}/comments` | Get all comments for an article | No | 200, 401, 422 |
| POST | `/articles/{slug}/comments` | Create a comment for an article | Yes | 200, 401, 422 |
| DELETE | `/articles/{slug}/comments/{id}` | Delete a comment | Yes | 200, 401, 422 |

### Tags

#### Tags API (`/api/tags`)

| Method | Endpoint | Description | Auth Required | Status Codes |
|--------|----------|-------------|---------------|--------------|
| GET | `/tags` | Get all tags | No | 200, 422 |

---

## Database Schema (UML)

### Entity Relationship Diagram

```
┌─────────────────────────────┐
│         User                │
├─────────────────────────────┤
│ - id: long                  │
│ - email: String             │
│ - username: String          │
│ - passwordHash: String      │
│ - bio: String               │
│ - image: String             │
├─────────────────────────────┤
│ + getId(): long             │
│ + setId(long): void         │
│ + getEmail(): String        │
│ + setEmail(String): void    │
│ + getUsername(): String     │
│ + setUsername(String): void │
│ + getPasswordHash(): String │
│ + setPasswordHash(String): void │
│ + getBio(): Optional<String>│
│ + setBio(String): void      │
│ + getImage(): Optional<String>│
│ + setImage(String): void    │
└─────────────────────────────┘
           △
           │
           │ ManyToOne (author)
           │
┌──────────┴──────────────────┐
│         Article             │
├─────────────────────────────┤
│ - id: long                  │
│ - slug: String              │
│ - title: String             │
│ - description: String       │
│ - body: String              │
│ - tags: Set<String>         │
│ - author: User              │
│ - createdAt: Instant        │
│ - updatedAt: Instant        │
├─────────────────────────────┤
│ + getId(): long             │
│ + setId(long): void         │
│ + getSlug(): String         │
│ + getTitle(): String        │
│ + setTitle(String): void    │
│ + getDescription(): String  │
│ + setDescription(String): void│
│ + getBody(): String         │
│ + setBody(String): void     │
│ + getTags(): Set<String>    │
│ + setTags(Set<String>): void│
│ + getAuthor(): User         │
│ + setAuthor(User): void     │
│ + getCreatedAt(): Instant   │
│ + setCreatedAt(Instant): void│
│ + getUpdatedAt(): Instant   │
│ + setUpdatedAt(Instant): void│
│ # onUpdate(): void          │
└─────────────────────────────┘
           △
           │
           │ ManyToOne (article)
           │
┌──────────┴──────────────────┐
│        Comment              │
├─────────────────────────────┤
│ - id: long                  │
│ - article: Article          │
│ - author: User              │
│ - body: String              │
│ - createdAt: Instant        │
│ - updatedAt: Instant        │
├─────────────────────────────┤
│ + getId(): long             │
│ + setId(long): void         │
│ + getArticle(): Article     │
│ + setArticle(Article): void │
│ + getAuthor(): User         │
│ + setAuthor(User): void     │
│ + getBody(): String         │
│ + setBody(String): void     │
│ + getCreatedAt(): Instant   │
│ + setCreatedAt(Instant): void│
│ + getUpdatedAt(): Instant   │
│ + setUpdatedAt(Instant): void│
└─────────────────────────────┘


┌─────────────────────────────┐
│   ArticleFavourite          │
├─────────────────────────────┤
│ - id: ArticleFavouriteId    │
│   ├─ userId: long           │
│   └─ articleId: long        │
├─────────────────────────────┤
│ + getId(): ArticleFavouriteId│
│ + setId(ArticleFavouriteId): void│
└─────────────────────────────┘


┌─────────────────────────────┐
│    FollowRelation           │
├─────────────────────────────┤
│ - id: FollowRelationId      │
│   ├─ followerId: long       │
│   └─ followeeId: long       │
├─────────────────────────────┤
│ + getId(): FollowRelationId │
│ + setId(FollowRelationId): void│
└─────────────────────────────┘
```

### Relationships

1. **User ← Article (ManyToOne)**
   - Each article has one author (User)
   - One user can have many articles
   - Foreign key: `Article.author` → `User.id`

2. **User ← Comment (ManyToOne)**
   - Each comment has one author (User)
   - One user can have many comments
   - Foreign key: `Comment.author` → `User.id`

3. **Article ← Comment (ManyToOne)**
   - Each comment belongs to one article
   - One article can have many comments
   - Foreign key: `Comment.article` → `Article.id`

4. **ArticleFavourite (Join Table)**
   - Many-to-Many relationship between User and Article for favorites
   - Composite primary key: `(userId, articleId)`
   - Represents which users favorited which articles

5. **FollowRelation (Join Table)**
   - Many-to-Many self-referencing relationship on User
   - Composite primary key: `(followerId, followeeId)`
   - Represents which users follow which other users
   - `followerId` → User who is following
   - `followeeId` → User who is being followed

### Table Descriptions

#### User
Stores user account information including authentication credentials and profile data.

**Columns:**
- `id` (PK): Auto-generated unique identifier
- `email`: User's email address (required, unique)
- `username`: User's username (required, unique)
- `passwordHash`: Hashed password (required)
- `bio`: User biography (optional)
- `image`: URL to user profile image (optional)

#### Article
Stores blog articles/posts created by users.

**Columns:**
- `id` (PK): Auto-generated unique identifier
- `slug`: URL-friendly identifier generated from title
- `title`: Article title (required)
- `description`: Short description of the article (required)
- `body`: Full article content (required)
- `tags`: Collection of tags associated with the article (stored as ElementCollection)
- `author` (FK): Reference to the User who created the article
- `createdAt`: Timestamp when article was created
- `updatedAt`: Timestamp when article was last updated (auto-updated via @PreUpdate)

#### Comment
Stores comments on articles.

**Columns:**
- `id` (PK): Auto-generated unique identifier
- `article` (FK): Reference to the Article being commented on
- `author` (FK): Reference to the User who created the comment
- `body`: Comment text content (required)
- `createdAt`: Timestamp when comment was created
- `updatedAt`: Timestamp when comment was last updated

#### ArticleFavourite
Junction table representing the many-to-many relationship between users and their favorited articles.

**Columns:**
- `userId` (PK, FK): Reference to User who favorited
- `articleId` (PK, FK): Reference to Article that was favorited

**Composite Primary Key:** `(userId, articleId)`

#### FollowRelation
Junction table representing the many-to-many relationship between users (followers and followees).

**Columns:**
- `followerId` (PK, FK): Reference to User who is following
- `followeeId` (PK, FK): Reference to User who is being followed

**Composite Primary Key:** `(followerId, followeeId)`

---

## Notes

- All endpoints use `/api` as the base path
- Authentication is implemented using Token-based authentication
- The API follows the [RealWorld](https://github.com/gothinkster/realworld) specification
- Generated controllers use OpenAPI/Swagger for API documentation
- The project follows Domain-Driven Design (DDD) principles with clear aggregate boundaries
