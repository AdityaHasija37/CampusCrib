# Changelog

## 2.0.0 (2026-11-02)

A rebuild of the Group 60 project. It keeps the team's design (one network, four enterprises, the same roles) and the stack (Java Swing on MySQL), and makes every role work end to end.

### Fixed from v1

- **Only logins were saved.** Listings, bookings and work requests lived in memory and disappeared on restart. Everything is now stored in the database.
- **No schema.** The app read a `users` table nobody could create. v2 ships migrations and creates its tables on first start.
- **Hard-coded database password** in `DatabaseConnection.java`. Settings now come from `campuscrib.properties` or environment variables.
- **Plain-text passwords** in the database. Now PBKDF2-SHA256 hashes.
- **Card numbers and CVVs** were modelled in `PaymentDetails`. Removed; payments are out of scope.
- **Broken routing.** Amenity requests went to a "Facility Manager" role that didn't exist, and maintenance relied on string matching that threw on unknown types. Routing is now typed and tested.
- **Roles that went nowhere.** Some dashboards opened as separate windows over the login screen, and the student role had no way back. Every role now has its own screens in one window.
- **Watermarked stock photos.** The demo listings used unlicensed stock previews (one showing an Adobe Stock watermark), with the same four images for every apartment. They're replaced by original illustrations, a different set for each listing.
- **Committed build output.** Compiled classes, `dist/` and NetBeans private files are gone; the project builds with Maven.

### Added

- Student search with filters and sorting, apartment photos, booking requests and their status.
- Agent listings with photos, list and unlist, and an inbox to approve or decline requests. Approving leases the apartment and declines competing requests in one transaction.
- Resident requests for maintenance, amenity bookings (no double-booking of a space) and inquiries, each with a full status history.
- Work queues for each service trade, and building-manager assignment.
- Reports: open work, urgent items, turnaround by trade, occupancy.
- Admin management of accounts, roles and access; change your own password.
- Demo data for every role, a zero-setup embedded database, Docker Compose for MySQL.
- 25 tests, run on H2 and on MySQL in CI.

## 1.0.0 (2024)

Original course project by Group 60: Aditya Hasija, Aravind Ravi, Nidhi Nair. Presentation: [docs/archive/campus-crib-presentation.pdf](docs/archive/campus-crib-presentation.pdf).
