# Decisions

### 1. Keep the team's design and stack
CampusCrib was a Group 60 project in Java Swing on MySQL, modelled as a network of enterprises and organizations. v2 keeps all of that, so it is still recognisably the team's project, and fixes what kept it from working.

### 2. Roles carry their enterprise and organization
v1 had a class per role and per organization with no behaviour. v2 uses one `Role` enum that knows its enterprise, organization and trade. Routing (`Role.forTrade`) and the admin's people list come straight from it.

### 3. Status transitions in one place
Which status can follow which lives in `RequestStatus.next`; who may make the move lives in `RequestService`. The UI shows exactly the actions `allowedNext` returns, so it can't offer something the service would refuse.

### 4. An embedded database for the demo, MySQL for real use
Anyone can run the jar with no setup and get a populated building. The same schema runs on MySQL, and CI runs the full test suite against both.

### 5. No migration framework
Six tables and one script don't justify Flyway or Liquibase. A forty-line runner applies versioned scripts once each.

### 6. Photos in the database
Listings and their photos are created, copied and deleted together. Storing images as blobs avoids a second storage system for a desktop app. Uploads are limited to 5 MB.

### 7. No payments
v1 modelled card numbers and CVVs in plain objects. Handling payment data needs a provider and compliance work, so v2 stops at booking approval.

### 8. Original illustrations instead of photos
The v1 demo photos were unlicensed stock previews. v2's demo listings use illustrations generated from code, so the repo has no licensing questions and each listing looks different.
