# Recommendation Service

## Objective & Scope
- Stateless aggregator. Calls movie-service and user-service, applies recommendation logic in-memory, and returns home page content.

## Out of Scope
- Performance optimizations (e.g. parallel upstream calls, caching).
- Upstream error handling. If movie-service or user-service fails, the error propagates.

## System Context & Data Flow

**movie-service** (property: `app.movie-service.base-url`):

| Endpoint | Response |
|---|---|
| `GET /api/movies/popular?limit={n}` | `List<MovieSummary>` |
| `GET /api/movies/latest?limit={n}` | `List<MovieSummary>` |
| `GET /api/movies/search?genre={genre}&limit={n}` | `List<MovieSummary>` |
| `GET /api/movies/batch?ids={ids}` | `List<MovieSummary>` |

**user-service** (property: `app.user-service.base-url`):

| Endpoint | Response |
|---|---|
| `GET /api/users/watchlist` | `List<Long>` (movieIds in the user's watchlist) |

The `X-User-Id` header from the incoming request must be forwarded on every call to user-service.

## API
- `GET /api/recommendations/home`
    - Returns `HomeResponse`.

## Core Business Logic & Rules

**Fixed genre list:** Action, Adventure, Animation, Comedy, Crime, Documentary, Drama, Family, Fantasy, History, Horror, Music, Mystery, Romance, Science Fiction, TV Movie, Thriller, War, Western

**Watchlist:**
1. `GET /api/users/watchlist` → `watchlistIds`
2. `GET /api/movies/batch?ids={watchlistIds}`
3. Populates `watchlist`

**Popular:**
1. `GET /api/movies/popular?limit=20`
2. Populates `popular`

**Latest:**
1. `GET /api/movies/latest?limit=20`
2. Populates `latest`

**By Genre:**
1. For each genre in the fixed list:
   - `GET /api/movies/search?genre={genre}&limit=20`
2. Populates `byGenre`

## Data Model

### API

**HomeResponse:**

| Field       | Type                              |
|-------------|-----------------------------------|
| watchlist   | `List<MovieSummary>`              |
| popular     | `List<MovieSummary>`              |
| latest      | `List<MovieSummary>`              |
| byGenre     | `Map<String, List<MovieSummary>>` |

**MovieSummary:**

| Field       | Type      |
|-------------|-----------|
| id           | Long      |
| title        | String    |
| posterPath   | String    |
| backdropPath | String    |
| voteAverage  | Double    |
| releaseDate  | LocalDate |
| genres       | String[]  |
