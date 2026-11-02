# Architecture

```
Swing UI (ui/)  ──calls──▶  services (service/)  ──▶  repositories (repo/)  ──JDBC──▶  MySQL 8 / H2
 screens per role            rules + permissions       SQL, prepared statements          schema via db/V1__schema.sql
```

- **The UI never writes SQL** and never decides permissions. It asks services, and services throw `ValidationException` (fixable input, shown as a list) or `AccessDeniedException`. `Ui.attempt` turns either into a dialog.
- **Services take the signed-in `User`** on every call that needs a permission check, so rules are testable without the UI.
- **One connection per call, transactions where it matters.** Approving a booking (lease the unit, decline the others) and every request status change (update plus history event) run in a single transaction.

## Data model

| Table | Notes |
| --- | --- |
| `users` | Role stored as text; enterprise and organization come from the `Role` enum. Residents carry a unit. Passwords are PBKDF2 hashes. |
| `apartments` | Listed by an agent; status `LISTED`, `LEASED` or `UNLISTED`. Amenities as a comma-separated list. |
| `apartment_photos` | Images stored in the database so a listing and its photos move together. |
| `booking_requests` | Student, apartment, move-in, lease length, message; `PENDING` until the agent decides. |
| `work_requests` | Category `MAINTENANCE` (with a trade), `AMENITY` (with a space and time slot) or `INQUIRY`; priority; status; optional assignee. |
| `request_events` | Every status change, with actor and note: the request's history. |
| `schema_version` | Which migrations have run. |

## Request lifecycle

Defined once in `RequestStatus.next(category)`; who may make each move is decided in `RequestService`.

| Category | Flow | Who moves it |
| --- | --- | --- |
| Maintenance | Submitted → Accepted → In progress → Completed | A crew member of the matching trade accepts (or a manager assigns); only the assignee can progress it |
| Amenity booking | Submitted → Approved or Declined | Building manager; approval fails if an approved booking for the same space overlaps |
| Inquiry | Submitted → Answered | Building manager, with the answer as the note |
| Any open request | → Cancelled | The resident who raised it, or a manager (with a note) |

Declining, answering and completing require a note, so the resident always gets a reason.

## Configuration

`AppConfig` reads `CAMPUSCRIB_DB_URL` / `_USER` / `_PASSWORD`, then `campuscrib.properties`, and otherwise uses an embedded H2 file in `~/.campuscrib` (MySQL mode). `Main` runs migrations and loads demo data into an empty database.

## Tests

`TestDb.fresh()` gives every test its own migrated database: H2 in memory, or MySQL when `CAMPUSCRIB_TEST_DB_URL` is set. Service tests use a fixed clock. `ScreensSmokeTest` signs in as every demo role headless and loads each screen. `ScreenshotTour` (run with `scripts/screenshots.sh`) renders the README screenshots from the real UI.
