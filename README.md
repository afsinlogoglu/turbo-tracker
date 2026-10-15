# Turbo

Takımların proje ve görevlerini yöneten Spring Boot REST API. React ekranı aynı endpoint'leri kullanır.

İlişki: bir `Team` birden fazla `Project` tutar, bir `Project` birden fazla `Task` tutar. Foreign key çocuk taraftadır (`projects.team_id`, `tasks.project_id`).

## Çalıştırma

```bash
cd backend
mvnw.cmd spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

Uygulama: [http://localhost:5173](http://localhost:5173)

Health: [http://localhost:8080/api/health](http://localhost:8080/api/health)

Dev database H2. Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
JDBC URL `jdbc:h2:mem:turbo`, user `sa`, password boş.

```bash
cd backend
mvnw.cmd test
```

## API

- `GET/POST /api/teams`, `GET/PATCH/DELETE /api/teams/{id}`
- `GET/POST /api/teams/{teamId}/projects`
- `GET/PATCH/DELETE /api/projects/{id}`
- `GET/POST /api/projects/{projectId}/tasks`
- `GET/PATCH/DELETE /api/tasks/{id}`

Kurallar:

- Aynı takım adı ikinci kez 409 döner. Aynı takımda aynı proje adı da 409.
- Projesi olan takım silinmez. Görevi olan proje silinmez. Önce çocuklar silinir.
- Olmayan kayıt 404. Boş isim 400.

## Prod

```bash
docker compose up --build
```

`prod` profile `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` okur. Tablolar `schema-postgres.sql` ile açılır, Hibernate `ddl-auto: validate` ile şemayı kontrol eder.

## Annotation'lar

- `@SpringBootApplication` uygulamayı başlatır: component scan, auto-configuration, embedded Tomcat.
- `@RestController` dönüşü JSON yapar. `@PathVariable` URL'deki id, `@RequestBody` JSON'u record'a çevirir.
- `@Service` iş kuralının durduğu bean. Constructor injection ile repository gelir.
- `@Transactional` method sonunda commit, hata olursa rollback. Okumada `readOnly = true`.
- `@Entity` tablo satırı. `@ManyToOne` ve `@JoinColumn` foreign key. `FetchType.LAZY` ilişkiyi ihtiyaç olunca yükler.
- `JpaRepository` `save` ve `findById` üretir. `findAllByTeamIdOrderByCreatedAtDesc` method adından sorgu kurar.
- `@Valid`, `@NotBlank`, `@Size` request'i controller'a girmeden reddeder.
- `@RestControllerAdvice` 400, 404 ve 409 cevaplarını tek JSON şeklinde toplar.
- `application.yml` dev (H2). `application-prod.yml` sadece `spring.profiles.active=prod` iken yüklenir.
