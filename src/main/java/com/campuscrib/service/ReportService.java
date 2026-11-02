package com.campuscrib.service;

import com.campuscrib.domain.Apartment;
import com.campuscrib.domain.ApartmentStatus;
import com.campuscrib.domain.Priority;
import com.campuscrib.domain.RequestCategory;
import com.campuscrib.domain.RequestStatus;
import com.campuscrib.domain.Role;
import com.campuscrib.domain.Trade;
import com.campuscrib.domain.User;
import com.campuscrib.domain.WorkRequest;
import com.campuscrib.repo.ApartmentRepository;
import com.campuscrib.repo.RequestRepository;
import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/** Building management's numbers: what's open, how fast each trade closes work, and occupancy. */
public class ReportService {
    private final RequestRepository requests;
    private final ApartmentRepository apartments;

    public ReportService(RequestRepository requests, ApartmentRepository apartments) {
        this.requests = requests;
        this.apartments = apartments;
    }

    public record TradeRow(Trade trade, long open, long completed, OptionalDouble averageHoursToComplete) {}

    public record Summary(
            long totalRequests,
            long openRequests,
            long urgentOpen,
            Map<RequestCategory, Long> openByCategory,
            List<TradeRow> trades,
            long listed,
            long leased,
            long unlisted) {}

    public Summary summary(User viewer, List<Long> agentIds) {
        if (viewer.role() != Role.BUILDING_MANAGER && viewer.role() != Role.ADMIN) {
            throw new AccessDeniedException("Reports are for building management.");
        }
        List<WorkRequest> all = requests.findAll();
        List<WorkRequest> open = all.stream().filter(r -> !r.status().isClosed()).toList();

        Map<RequestCategory, Long> byCategory = new EnumMap<>(RequestCategory.class);
        for (RequestCategory c : RequestCategory.values()) byCategory.put(c, open.stream().filter(r -> r.category() == c).count());

        List<TradeRow> trades = java.util.Arrays.stream(Trade.values()).map(t -> {
            List<WorkRequest> forTrade = all.stream().filter(r -> r.trade() == t).toList();
            List<WorkRequest> done = forTrade.stream().filter(r -> r.status() == RequestStatus.COMPLETED && r.closedAt() != null).toList();
            OptionalDouble avg = done.stream().mapToDouble(r -> Duration.between(r.createdAt(), r.closedAt()).toMinutes() / 60.0).average();
            return new TradeRow(t, forTrade.stream().filter(r -> !r.status().isClosed()).count(), done.size(), avg);
        }).toList();

        List<Apartment> listings = agentIds.stream().flatMap(id -> apartments.findByAgent(id).stream()).toList();
        return new Summary(
                all.size(),
                open.size(),
                open.stream().filter(r -> r.priority() == Priority.URGENT).count(),
                byCategory,
                trades,
                listings.stream().filter(a -> a.status() == ApartmentStatus.LISTED).count(),
                listings.stream().filter(a -> a.status() == ApartmentStatus.LEASED).count(),
                listings.stream().filter(a -> a.status() == ApartmentStatus.UNLISTED).count());
    }
}
