# Learning Notes

This file collects the concept explanations from the build, organized by topic, in the format you asked for: **CONCEPT / WHY / SIMPLE EXAMPLE / IN OUR PROJECT**. The same explanations also live as comments directly in the source files, right next to the code they describe — read this file for the overview, then open the referenced file to see it in context.

Everything here assumes zero prior Spring Boot knowledge, as requested.

---

## 1. Java language concepts

### Classes and objects
**CONCEPT:** A class is a blueprint; an object is a specific instance built from that blueprint, with its own copy of the fields.
**WHY:** Modeling real things (a `User`, an `Expense`) as objects lets us group related data and behavior together instead of passing loose variables around.
**SIMPLE EXAMPLE:**
```java
class Point { int x; int y; }
Point p = new Point(); // p is an object; Point is the class
```
**IN OUR PROJECT:** `User`, `Expense`, `Category`, `Budget` (all in their respective `entity/` packages) are classes; each row fetched from the database becomes one object.

### Constructors
**CONCEPT:** A special method, same name as the class, that runs when you create an object with `new`, setting up its initial state.
**WHY:** Guarantees an object starts in a valid state — you can't end up with half-initialized data.
**IN OUR PROJECT:** Lombok's `@NoArgsConstructor` and `@AllArgsConstructor` generate constructors for us on entities (JPA specifically *requires* a no-args constructor so Hibernate can instantiate entities via reflection before populating fields). `@RequiredArgsConstructor` on services generates a constructor for all `final` fields — this is how Spring's dependency injection actually happens (see "Dependency Injection" below).

### Interfaces
**CONCEPT:** A contract listing method signatures with no implementation. A class `implements` an interface by providing bodies for those methods.
**WHY:** Code that depends on an interface doesn't care *how* the work happens, only *that* it happens — this is what makes swapping implementations (or mocking for tests) possible.
**SIMPLE EXAMPLE:**
```java
interface Shape { double area(); }
class Circle implements Shape {
    double radius;
    public double area() { return Math.PI * radius * radius; }
}
```
**IN OUR PROJECT:** `UserRepository extends JpaRepository<User, Long>` — `JpaRepository` is an interface; we never write an implementation class, Spring Data JPA generates one at startup. `UserDetailsService` (implemented by `CustomUserDetailsService`) is a Spring Security interface — Spring Security calls it without knowing how our users are actually stored.

### Inheritance
**CONCEPT:** A class can `extend` another, inheriting its fields/methods and optionally overriding them.
**WHY:** Share common behavior instead of duplicating it.
**IN OUR PROJECT:** All our custom exceptions (`ResourceNotFoundException`, `UnauthorizedException`, etc.) extend `RuntimeException`. `JwtAuthenticationFilter extends OncePerRequestFilter`, inheriting the guarantee that its logic runs exactly once per request.

### Encapsulation
**CONCEPT:** Keeping a class's internal fields private and only exposing controlled access via methods (getters/setters, or better, well-named behavior methods).
**WHY:** Prevents external code from putting an object into an invalid state, and lets you change internals later without breaking callers.
**IN OUR PROJECT:** Entity fields are `private`; Lombok's `@Getter`/`@Setter` generate the controlled access points. DTOs (records) take this further — they're immutable, so once constructed their values can never change at all.

### Enums
**CONCEPT:** A fixed, named set of constant values.
**WHY:** Type safety over raw strings — the compiler catches `Role.ADMIN` typos that `"ADMIN"` (a plain string) wouldn't.
**IN OUR PROJECT:** `Role` (`user/entity/Role.java`) has exactly `USER` and `ADMIN`. Stored in the DB as `@Enumerated(EnumType.STRING)` so the column holds readable text ("USER"), not a fragile numeric index.

### Collections
**CONCEPT:** Built-in data structures — `List`, `Map`, `Set` — for holding groups of objects.
**IN OUR PROJECT:** `User.expenses` is a `List<Expense>`; `GlobalExceptionHandler` builds a `Map<String, String>` of field-name → error-message when validation fails.

### Generics
**CONCEPT:** Type parameters (`<T>`) that let one class or method work with any type while staying type-checked at compile time.
**WHY:** Without generics, `ApiResponse` would either need a separate class per data type, or use `Object` and lose all compile-time type safety.
**SIMPLE EXAMPLE:**
```java
class Box<T> { T value; }
Box<String> b = new Box<>(); // T becomes String for this instance
```
**IN OUR PROJECT:** `ApiResponse<T>` wraps any response payload type. `JpaRepository<User, Long>` is generic over the entity type and its primary-key type.

### Streams
**CONCEPT:** A functional-style pipeline for processing collections — `.stream().filter(...).map(...).toList()` — without manual loops and mutable accumulator variables.
**WHY:** More declarative and less error-prone than hand-written loops, especially for transform-and-collect operations.
**IN OUR PROJECT:** `CategoryService.getAll()` does `categoryRepository.findAllByUserIdOrderByNameAsc(id).stream().map(this::toResponse).toList()` — fetch entities, convert each to a DTO, done in one expression.

### Optional
**CONCEPT:** A wrapper type representing "a value that might be absent," used instead of returning `null`.
**WHY:** Forces the caller to explicitly handle the absent case (`.orElseThrow(...)`) rather than risking a `NullPointerException` somewhere downstream.
**IN OUR PROJECT:** `UserRepository.findByEmail(String)` returns `Optional<User>`; every service does `.orElseThrow(() -> new ResourceNotFoundException(...))` to turn "not found" into a proper 404 instead of a null-pointer crash.

### Exception handling
**CONCEPT:** Java separates *checked* exceptions (must be declared/caught, e.g. `IOException`) from *unchecked* ones (`RuntimeException` and subclasses, no declaration required).
**WHY WE USE UNCHECKED HERE:** Business conditions like "expense not found" are expected and recoverable, not catastrophic — we don't want `throws ResourceNotFoundException` cluttering every method signature up the call stack. Instead, we throw it anywhere and catch it *once*, centrally.
**IN OUR PROJECT:** `common/exception/*.java` define the custom exception types; `GlobalExceptionHandler` (`@RestControllerAdvice`) catches them app-wide and converts each to the right HTTP status + a consistent JSON error body.

### Lambdas
**CONCEPT:** An anonymous, inline function — `(params) -> expression`.
**WHY:** Lets you pass behavior as a value (e.g. "what to do if this Optional is empty") without writing a whole named class.
**IN OUR PROJECT:** `.orElseThrow(() -> new ResourceNotFoundException("User not found"))` everywhere; the JPA `Specification` lambdas in `ExpenseSpecifications.java` (`(root, query, cb) -> cb.equal(...)`).

### Records
**CONCEPT:** A compact syntax for an immutable data-carrier class — declares fields, and the compiler generates the constructor, accessors, `equals()`, `hashCode()`, and `toString()`.
**WHY:** DTOs are exactly this shape (bag of immutable fields, no behavior) — records eliminate the boilerplate a traditional class would need.
**IN OUR PROJECT:** Every request/response DTO (`RegisterRequest`, `ExpenseResponse`, `ApiResponse<T>`, etc.) is a record.

---

## 2. Spring Boot & backend concepts

### What Spring Boot is, and why
Spring Boot is a framework that removes most of the manual wiring the older "Spring Framework" required. Two headline features:
- **Auto-configuration** — because `spring-boot-starter-web` is on the classpath, Spring Boot automatically configures and starts an embedded Tomcat server, sets up Jackson for JSON, etc. — no XML, no manual `web.xml`.
- **Dependency Injection (DI) / Inversion of Control (IoC)** — see below.

**Starters** (`spring-boot-starter-web`, `-security`, `-data-jpa`...) are curated dependency bundles: adding one pulls in everything commonly needed for that concern, at compatible versions.

### Dependency Injection
**CONCEPT:** Instead of a class creating its own dependencies (`new UserRepository()`), it declares what it needs, and a framework ("container") supplies them.
**WHY:** Decouples classes from *how* their dependencies are built, and makes testing trivial — swap in a fake implementation without touching the class itself.
**SIMPLE EXAMPLE (no framework):**
```java
class OrderService {
    private final PaymentGateway gateway;
    OrderService(PaymentGateway gateway) { this.gateway = gateway; } // dependency handed in, not created here
}
```
**WHY CONSTRUCTOR injection (not field injection with `@Autowired` on the field):**
1. Dependencies are `final` — impossible to forget one; the object literally can't exist without them.
2. Trivial to unit test: `new AuthService(mockRepo, mockEncoder, ...)` with no Spring container needed at all.
3. No hidden circular-dependency surprises that only appear at runtime.

**IN OUR PROJECT:** Every service/controller uses Lombok's `@RequiredArgsConstructor`, which generates a constructor for all `final` fields — Spring sees that constructor and automatically supplies a matching bean for each parameter.

### Annotations used throughout, explained

| Annotation | What it means |
|---|---|
| `@SpringBootApplication` | Marks the entry-point class; combines `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| `@RestController` | `@Controller` + `@ResponseBody` — every method's return value is serialized straight to JSON, not resolved to an HTML view |
| `@Service` | Marks a class as a Spring-managed bean holding business logic |
| `@Repository` | Marks a data-access bean (Spring Data JPA generates these for interfaces automatically) |
| `@Entity` | Marks a class as mapped to a database table (JPA/Hibernate) |
| `@Autowired` | Requests dependency injection — we mostly avoid this in favor of constructor injection, which doesn't need the annotation at all |
| `@Transactional` | Wraps a method in a database transaction — if an exception is thrown partway through, every change in that method is rolled back, not partially applied |
| `@Valid` | Tells Spring to run Bean Validation on this parameter (checking `@NotBlank`, `@Email`, etc. from the DTO) before the method body runs |
| `@RequestBody` | Deserializes the incoming JSON body into a Java object |
| `@PathVariable` / `@RequestParam` | Binds a URL path segment (`/expenses/{id}`) or query string parameter (`?page=0`) to a method parameter |

### REST APIs & HTTP
**CONCEPT:** REST models each URL as a "resource" (e.g. `/expenses/5` is *the expense with id 5*), and HTTP methods express the action:
- `GET` — read, no side effects, safe to retry/cache
- `POST` — create a new resource (or, for `/auth/login`, "do something with side effects that isn't idempotent")
- `PUT` — replace/update an existing resource
- `DELETE` — remove it

**HTTP status codes we use:** `200 OK`, `201 Created` (after POST), `400 Bad Request` (validation failure), `401 Unauthorized` (missing/invalid credentials or token), `403 Forbidden` (authenticated, but not allowed), `404 Not Found`, `409 Conflict` (duplicate resource), `500 Internal Server Error` (caught by our fallback handler so raw stack traces never leak to the client).

**API versioning:** every route is under `/api/v1/...` so we could introduce breaking changes in a future `/api/v2` without breaking existing frontend builds still pointed at v1.

### JPA, Hibernate, and ORM
**CONCEPT:** An **ORM** (Object-Relational Mapper) lets you work with plain Java objects instead of hand-written SQL for every operation. **JPA** is the *specification* (a set of interfaces/annotations like `@Entity`, `@Id`); **Hibernate** is the actual *implementation* that does the work (generates SQL, manages the object-to-row mapping, tracks changes).
**WHY:** Writing raw JDBC/SQL for every CRUD operation is repetitive and error-prone (string-built SQL is also an injection risk). JPA lets you write `expenseRepository.save(expense)` and Hibernate handles the `INSERT`/`UPDATE` SQL.
**Entity mapping:** `@Entity` + `@Table` map a class to a table; `@Id` + `@GeneratedValue` mark the primary key and how it's generated (`IDENTITY` = auto-increment column, delegated to MySQL).
**Relationships:**
- `@ManyToOne` — many `Expense` rows point to one `User` (foreign key `user_id` lives on the `expenses` table)
- `@OneToMany(mappedBy = "user")` — the inverse side; `User.expenses` is a *view* of that same foreign key, not a separate column
- `FetchType.LAZY` on `@ManyToOne` means Hibernate doesn't actually query the related `User`/`Category` row until you call `.getUser()`/`.getCategory()` — avoids unnecessary joins when you only need the expense's own fields.

**Repositories:** `interface ExpenseRepository extends JpaRepository<Expense, Long>` gets `save`, `findById`, `findAll`, `deleteById`, etc. for free. **Derived query methods** like `findByEmail(String email)` are parsed from the method *name* itself — no SQL or annotation needed for straightforward lookups.

**Indexes & constraints:** `Expense` has a composite index on `(user_id, expenseDate)` (`@Index` in `@Table`) because "this user's expenses in this date range" is the most common query shape — without an index, MySQL would have to scan every row.

**Why `BigDecimal`, never `float`/`double`, for money:** floating-point types store numbers in binary, and many ordinary decimal fractions (like `0.1`) can't be represented *exactly* in binary — small rounding errors creep in and compound (`0.1 + 0.2 != 0.3` in floating point). `BigDecimal` stores an exact, arbitrary-precision decimal, so `19.99 + 0.01` is *exactly* `20.00`. We also pin the database column to `DECIMAL(12,2)` so precision isn't silently lost at the storage layer either.

---

## 3. Authentication & security concepts

### Authentication vs. authorization
- **Authentication** answers "who are you?" (logging in, proving identity)
- **Authorization** answers "are you allowed to do this?" (role checks, ownership checks)

### JWT (JSON Web Token)
**CONCEPT:** A compact, digitally signed string encoding claims (e.g. "subject: alice@example.com", "expires: <timestamp>"). Format: `header.payload.signature`.
**WHY JWT over server-side sessions:** a session requires the server to store state (in memory or a DB) and look it up on every request. A JWT is **stateless** — the server verifies authenticity via the cryptographic signature, no DB round-trip needed, which scales better across multiple server instances. Trade-off: a JWT can't be revoked early on its own — this is exactly why we pair it with a **refresh token**.

**Access token vs. refresh token:**
- **Access token** — short-lived (15 minutes here), sent on every request via `Authorization: Bearer <token>`, never stored server-side.
- **Refresh token** — long-lived (7 days), stored in the `refresh_tokens` table (so it *can* be revoked/rotated), used only to mint a new access token.

**Refresh token rotation:** every `/auth/refresh` call marks the used refresh token `revoked = true` and issues a brand-new pair. If a stolen refresh token is used once by an attacker, the legitimate user's next refresh invalidates it — limiting the exploitation window.

### BCrypt and password hashing
**CONCEPT:** `BCryptPasswordEncoder` hashes passwords with a computationally expensive, *salted* algorithm — a random value is mixed in before hashing, so identical passwords produce different hashes, and the process can't be reversed (only compared: `encoder.matches(raw, hash)`).
**WHY:** if the database ever leaks, plain-text passwords expose every user's real password immediately. BCrypt makes brute-forcing the hash computationally expensive even with the hash in hand.
**Rule followed everywhere in this project:** passwords are never logged, never returned in any API response DTO, and never stored anywhere except as a BCrypt hash.

### Spring Security & security filters
**CONCEPT:** Spring Security is built as a *chain* of filters — each intercepts every incoming request before it reaches a controller, does one job, and passes control along. `JwtAuthenticationFilter` is our custom link in that chain: it reads the `Authorization` header, validates the JWT, and — if valid — tells Spring Security "this request is authenticated as this user" by populating `SecurityContextHolder`.
**Why endpoints need this:** `SecurityConfig` marks `/api/v1/auth/**` as `permitAll()` (you can't present a JWT before you have one) and everything else as `.authenticated()` — the filter chain rejects unauthenticated requests to protected routes with a 401 before they ever reach a controller method.

**Ownership checks:** authentication alone isn't enough — every service method (e.g. `ExpenseService.getById`) looks the resource up scoped to the *current* user's ID (`findByIdAndUserId`), so even a valid, authenticated user can never fetch another user's data by guessing an ID.

---

## 4. Frontend concepts

### React
**CONCEPT:** A library for building UIs out of **components** — small, reusable functions that take **props** (input data) and return what should be on screen. **State** (`useState`) is data a component remembers between renders; when state changes, React re-renders. **Effects** (`useEffect`) run side effects (like syncing a form when data loads) in response to state/prop changes.
**IN OUR PROJECT:** `ExpenseFormPage` uses `useEffect` to reset the form's fields once the existing expense data has loaded from the API (edit mode).

### TypeScript
**CONCEPT:** A superset of JavaScript that adds static types, checked at compile time (not runtime).
**WHY with React:** catches whole classes of bugs (passing a `string` where a component expects a `number`) before the code ever runs, and gives autocomplete/refactoring safety across a codebase this size.
**IN OUR PROJECT:** every API payload has a TypeScript `interface` (e.g. `Expense`, `Budget` in each feature's `types/` folder) so a typo in a field name is a compile error, not a silent runtime bug.

### TanStack Query — "server state" vs. "client state"
**CONCEPT:** Not all React state is the same. **Client state** is UI-only (is this modal open?) — plain `useState` is fine. **Server state** is data that actually lives on the backend (expenses, categories) and can go stale, needs refetching, retries, and de-duplication of in-flight requests.
**Queries** (`useQuery`) are for *reading* — given a `queryKey`, TanStack Query caches the result and automatically refetches when the key changes (e.g. filters change → new page of expenses). **Mutations** (`useMutation`) are for *writing* (create/update/delete); on success we call `queryClient.invalidateQueries` to mark the relevant cached data stale, so any visible list automatically refetches — no manual page reload.
**IN OUR PROJECT:** `features/expenses/hooks/useExpenses.ts` is the clearest example — `useExpenses(filters)` re-fetches automatically whenever `filters` changes.

### Zod + React Hook Form
**CONCEPT:** Zod defines a data **schema**; TypeScript types are *derived* from it (`z.infer<typeof schema>`) so validation rules and types can never drift apart. React Hook Form manages form state with minimal re-renders; `zodResolver` plugs the Zod schema in as its validation engine.
**IN OUR PROJECT:** every form (`LoginPage`, `RegisterPage`, `ExpenseFormPage`) defines its schema at the top of the file, right above the component.

### Axios & interceptors
**CONCEPT:** An HTTP client wrapping the browser's fetch API with nicer defaults. An **interceptor** is a function that runs on every request or response passing through one configured instance.
**WHY a centralized client:** one file (`lib/axios.ts`) owns the base URL and all auth logic — components never think about tokens directly. The **request interceptor** attaches the current access token to every outgoing call. The **response interceptor** catches `401`s, transparently calls `/auth/refresh`, retries the original failed request with the new token, and only redirects to `/login` if the refresh itself fails — token expiry becomes invisible to the rest of the app.

### Feature-based folder structure
**WHY not "group everything by file type"** (all components in one folder, all API calls in another): as an app grows, working on "expenses" would mean jumping between five unrelated top-level folders. Grouping by *feature* (`features/expenses/{api,hooks,pages,components,types}`) keeps everything about one concern physically close together, and makes it obvious where new expense-related code belongs.

---

## 5. Testing, scheduling, and authorization additions

### Unit testing with Mockito (no Spring context)
**CONCEPT:** `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks` lets you construct a service with fake dependencies and control exactly what they return, without starting a Spring application context at all.
**WHY it matters here specifically:** this is the direct payoff of constructor injection (see section 2). Because `CategoryService`'s dependencies arrive via its constructor, a test can call `new CategoryService(mockRepo, mockUserRepo)` — or let `@InjectMocks` do that wiring — and the test runs in milliseconds with zero database, zero HTTP server, zero Spring startup cost. Field injection (`@Autowired` on a field) would make this much harder, since there'd be no constructor to hand mocks to.
**IN OUR PROJECT:** `backend/src/test/java/.../CategoryServiceTest.java` and `AuthServiceTest.java`. The `AuthServiceTest` password test is the one most worth reading closely — it asserts the *saved* user's password is the bcrypt hash, never the plain-text value, which is exactly the property BCrypt is there to guarantee.

### @Scheduled tasks
**CONCEPT:** `@Scheduled(fixedRate = ...)` on a method tells Spring's built-in scheduler to invoke it automatically on a timer, instead of it being triggered by an incoming HTTP request. Requires `@EnableScheduling` on the main application class to activate the scheduler thread pool at all — without it, `@Scheduled` methods are silently never called.
**WHY:** some work isn't naturally tied to a user action. Nobody sends a request that means "please clean up old refresh tokens" — it just needs to happen periodically in the background.
**IN OUR PROJECT:** `auth/scheduled/RefreshTokenCleanupJob.java` runs once every 24 hours and deletes `refresh_tokens` rows that are expired or already revoked.
**COMMON MISTAKE:** forgetting `@EnableScheduling` — the method compiles fine, the app starts fine, and the job simply never runs, with no error anywhere.

### Path-based vs. resource-level authorization
**CONCEPT:** Authorization can be enforced at different levels. `SecurityConfig`'s `.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")` is **path-based** — it's checked before the request even reaches a controller. A different technique, method-level security (`@PreAuthorize("hasRole('ADMIN')")` on a controller method), is checked at the method call itself and can express finer-grained rules (e.g. "only if this is also *your own* resource").
**WHY path-based here:** the admin endpoint's entire *purpose* is "admin-only," with no per-request nuance beyond that — a blanket path rule is simpler and just as correct, and it's easier to see the whole authorization policy in one place (`SecurityConfig`) rather than scattered across annotations on individual methods.
**IN OUR PROJECT:** `AdminController.listUsers()` has no security annotation on it at all — the protection lives entirely in `SecurityConfig`. Compare this to the *ownership* checks in `ExpenseService`/`CategoryService`/`BudgetService`, which can't be expressed as a URL pattern at all (there's no way to know from the URL alone whether expense #47 belongs to the caller) — those are necessarily checked in application code via `findByIdAndUserId`.

### Fixed-window rate limiting
**CONCEPT:** Track how many requests a client has made in the current time window (60 seconds, here); once they exceed a threshold, reject further requests with `429 Too Many Requests` until the window resets.
**WHY on `/auth/**` specifically:** login is the one endpoint where an attacker benefits from unlimited retries (guessing passwords). Rate limiting doesn't stop a determined, distributed attacker, but it meaningfully raises the cost of naive brute-forcing from a single source.
**IN OUR PROJECT:** `security/RateLimitingFilter.java` — deliberately simple (in-memory `ConcurrentHashMap`, single JVM) so the *concept* is visible without a Redis dependency. The class comment explains exactly why this specific implementation wouldn't be sufficient once you have more than one backend instance running behind a load balancer.

---

## Common mistakes to watch for

- **Storing money as `double`** instead of `BigDecimal` — looks fine in testing, silently drifts in production.
- **Field injection (`@Autowired` on a field)** instead of constructor injection — works, but hides required dependencies and makes unit testing without a full Spring context much harder.
- **Returning JPA entities directly from a controller** instead of mapping to a DTO — leaks internal fields (like a password hash) and couples your public API to your database schema.
- **Forgetting `FetchType.LAZY`** on `@ManyToOne`/`@OneToMany` — defaults can trigger expensive, unnecessary joins on every entity load.
- **Trusting a JWT's claims without verifying the signature** — always let a library (`jjwt` here) verify and parse; never manually decode the payload and trust it.
- **Skipping the ownership check** (`findByIdAndUserId` vs. plain `findById`) — the #1 way multi-tenant apps leak one user's data to another.
- **Forgetting `enabled: !!id` on a TanStack Query hook that depends on an optional parameter** — otherwise it fires a request with `id: undefined` before the value is ready.
