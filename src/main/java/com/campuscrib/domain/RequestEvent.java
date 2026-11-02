package com.campuscrib.domain;

import java.time.Instant;

/** One step in a request's history: who moved it, from what, to what, and why. */
public record RequestEvent(long id, long requestId, String actorName, Role actorRole, RequestStatus fromStatus, RequestStatus toStatus, String note, Instant createdAt) {}
