-- CampusCrib schema. Runs on MySQL 8 and on H2 in MySQL mode (demo and tests).

CREATE TABLE users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(40)  NOT NULL UNIQUE,
  password_hash VARCHAR(200) NOT NULL,
  full_name     VARCHAR(100) NOT NULL,
  email         VARCHAR(120) NOT NULL,
  role          VARCHAR(32)  NOT NULL,
  unit          VARCHAR(20),
  active        BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE apartments (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  agent_id            BIGINT        NOT NULL,
  code                VARCHAR(20)   NOT NULL,
  title               VARCHAR(120)  NOT NULL,
  neighborhood        VARCHAR(60)   NOT NULL,
  address             VARCHAR(160)  NOT NULL,
  description         VARCHAR(2000) NOT NULL,
  monthly_rent        DECIMAL(10,2) NOT NULL,
  bedrooms            INT           NOT NULL,
  bathrooms           INT           NOT NULL,
  distance_km         DECIMAL(4,1)  NOT NULL,
  amenities           VARCHAR(500)  NOT NULL DEFAULT '',
  available_from      DATE          NOT NULL,
  status              VARCHAR(16)   NOT NULL DEFAULT 'LISTED',
  created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_apartment_agent FOREIGN KEY (agent_id) REFERENCES users(id)
);

CREATE TABLE apartment_photos (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  apartment_id BIGINT      NOT NULL,
  caption      VARCHAR(60) NOT NULL,
  position     INT         NOT NULL,
  image        LONGBLOB    NOT NULL,
  CONSTRAINT fk_photo_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE CASCADE
);

CREATE TABLE booking_requests (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  apartment_id BIGINT        NOT NULL,
  student_id   BIGINT        NOT NULL,
  move_in      DATE          NOT NULL,
  lease_months INT           NOT NULL,
  message      VARCHAR(1000) NOT NULL DEFAULT '',
  status       VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
  agent_note   VARCHAR(1000) NOT NULL DEFAULT '',
  created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  decided_at   TIMESTAMP     NULL,
  CONSTRAINT fk_booking_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE CASCADE,
  CONSTRAINT fk_booking_student FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE TABLE work_requests (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  resident_id  BIGINT        NOT NULL,
  category     VARCHAR(16)   NOT NULL,
  trade        VARCHAR(16)   NULL,
  amenity      VARCHAR(20)   NULL,
  slot_start   TIMESTAMP     NULL,
  slot_hours   INT           NULL,
  title        VARCHAR(120)  NOT NULL,
  description  VARCHAR(2000) NOT NULL,
  unit         VARCHAR(20)   NOT NULL,
  priority     VARCHAR(10)   NOT NULL,
  status       VARCHAR(16)   NOT NULL,
  assignee_id  BIGINT        NULL,
  created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  closed_at    TIMESTAMP     NULL,
  CONSTRAINT fk_request_resident FOREIGN KEY (resident_id) REFERENCES users(id),
  CONSTRAINT fk_request_assignee FOREIGN KEY (assignee_id) REFERENCES users(id)
);

CREATE TABLE request_events (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id  BIGINT        NOT NULL,
  actor_id    BIGINT        NOT NULL,
  from_status VARCHAR(16)   NULL,
  to_status   VARCHAR(16)   NOT NULL,
  note        VARCHAR(1000) NOT NULL DEFAULT '',
  created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_event_request FOREIGN KEY (request_id) REFERENCES work_requests(id) ON DELETE CASCADE,
  CONSTRAINT fk_event_actor FOREIGN KEY (actor_id) REFERENCES users(id)
);

CREATE INDEX idx_apartment_status ON apartments(status);
CREATE INDEX idx_booking_apartment ON booking_requests(apartment_id);
CREATE INDEX idx_booking_student ON booking_requests(student_id);
CREATE INDEX idx_request_status ON work_requests(status);
CREATE INDEX idx_request_resident ON work_requests(resident_id);
CREATE INDEX idx_event_request ON request_events(request_id);
