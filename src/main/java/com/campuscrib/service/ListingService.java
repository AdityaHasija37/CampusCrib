package com.campuscrib.service;

import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentDraft;
import com.campuscrib.domain.ApartmentStatus;
import com.campuscrib.domain.Photo;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.SearchCriteria;
import com.campuscrib.domain.User;
import com.campuscrib.repo.ApartmentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Agents manage their own listings; everyone signed in can search what's listed. */
public class ListingService {
    static final int MAX_PHOTO_BYTES = 5 * 1024 * 1024;
    private final ApartmentRepository apartments;

    public ListingService(ApartmentRepository apartments) {
        this.apartments = apartments;
    }

    public List<Apartment> search(SearchCriteria criteria) {
        return apartments.search(criteria);
    }

    public Apartment get(long id) {
        return apartments.findById(id).orElseThrow(() -> new ValidationException("That apartment no longer exists."));
    }

    public List<Photo> photos(long apartmentId) {
        return apartments.photos(apartmentId);
    }

    public Optional<byte[]> cover(long apartmentId) {
        return apartments.coverPhoto(apartmentId);
    }

    public List<Apartment> myListings(User agent) {
        requireAgent(agent);
        return apartments.findByAgent(agent.id());
    }

    public Apartment create(User agent, ApartmentDraft draft) {
        requireAgent(agent);
        validate(draft);
        return get(apartments.insert(agent.id(), draft));
    }

    public Apartment update(User agent, long id, ApartmentDraft draft) {
        Apartment existing = owned(agent, id);
        validate(draft);
        apartments.update(existing.id(), draft);
        return get(id);
    }

    /** Agents can take a listing off the market or relist it; LEASED is set by approving a booking. */
    public void setListed(User agent, long id, boolean listed) {
        Apartment a = owned(agent, id);
        apartments.setStatus(a.id(), listed ? ApartmentStatus.LISTED : ApartmentStatus.UNLISTED);
    }

    public void delete(User agent, long id) {
        apartments.delete(owned(agent, id).id());
    }

    public void addPhoto(User agent, long apartmentId, String caption, byte[] image) {
        owned(agent, apartmentId);
        if (image == null || image.length == 0) throw new ValidationException("Choose an image file.");
        if (image.length > MAX_PHOTO_BYTES) throw new ValidationException("Photos must be 5 MB or smaller.");
        apartments.addPhoto(apartmentId, Problems.blank(caption) ? "Photo" : caption.strip(), image);
    }

    public void deletePhoto(User agent, Photo photo) {
        owned(agent, photo.apartmentId());
        apartments.deletePhoto(photo.id());
    }

    private Apartment owned(User agent, long id) {
        requireAgent(agent);
        Apartment a = get(id);
        if (a.agentId() != agent.id()) throw new AccessDeniedException("You can only change your own listings.");
        return a;
    }

    private static void requireAgent(User user) {
        if (user.role() != Role.AGENT) throw new AccessDeniedException("Only real estate agents manage listings.");
    }

    static void validate(ApartmentDraft d) {
        new Problems()
                .check(!Problems.blank(d.code()) && d.code().strip().length() <= 20, "Give the unit a code of up to 20 characters, e.g. A101.")
                .check(!Problems.blank(d.title()) && d.title().strip().length() <= 120, "Add a title (up to 120 characters).")
                .check(!Problems.blank(d.neighborhood()), "Add the neighborhood.")
                .check(!Problems.blank(d.address()), "Add the street address.")
                .check(!Problems.blank(d.description()) && d.description().strip().length() >= 20, "Describe the apartment in at least 20 characters.")
                .check(d.monthlyRent() != null && d.monthlyRent().compareTo(BigDecimal.valueOf(100)) >= 0 && d.monthlyRent().compareTo(BigDecimal.valueOf(20000)) <= 0,
                        "Monthly rent must be between $100 and $20,000.")
                .check(d.bedrooms() >= 0 && d.bedrooms() <= 10, "Bedrooms must be between 0 (studio) and 10.")
                .check(d.bathrooms() >= 1 && d.bathrooms() <= 6, "Bathrooms must be between 1 and 6.")
                .check(d.distanceKm() != null && d.distanceKm().signum() >= 0 && d.distanceKm().compareTo(BigDecimal.valueOf(50)) <= 0,
                        "Distance to campus must be between 0 and 50 km.")
                .check(d.availableFrom() != null, "Set the date it's available from.")
                .throwIfAny();
    }
}
