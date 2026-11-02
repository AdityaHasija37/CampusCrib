package com.campuscrib.domain;

public record Photo(long id, long apartmentId, String caption, int position, byte[] image) {}
