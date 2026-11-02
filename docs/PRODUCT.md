# Product brief

## Problem

From the team's original problem statement: thousands of students look for accommodation every year under tight constraints (budget, distance to campus, neighborhood, amenities), with no portal built around them. Once they move in, the problems shift: maintenance and services are fragmented, and residents can't easily reach the person who can fix things.

The underlying issue is **handoffs**. The same building involves students, leasing agents, residents, building managers and several service crews. Each needs its own view, its own permissions, and a clear handover to the next person.

## Who it's for

| Person | What they need |
| --- | --- |
| Student | Compare apartments on what matters and ask for one without phone tag |
| Real estate agent | Keep listings current and decide on requests quickly, without double-leasing |
| Resident | Report a problem or book a space once, and see where it stands |
| Building manager | See everything open, push work to the right crew, and know how long things take |
| Service crew | Only the jobs for their trade, and a simple way to move each one forward |
| Admin | Control who has access to what |

## What CampusCrib does

1. **Search and book.** Filters on rent, bedrooms, distance and amenities; photos and details; a booking request with move-in date and lease length.
2. **Decide.** Agents approve or decline with a note. Approving leases the unit and closes other pending requests for it.
3. **Request.** Residents pick what they need: something broken (by trade), a shared space (with a time slot), or a question.
4. **Route.** Maintenance goes to the crew for that trade; bookings and questions go to building management. Managers can assign a job to a specific person.
5. **Track.** Every change is recorded with who made it and why, and shown as a timeline.
6. **Measure.** Open and urgent work, turnaround by trade, occupancy.

## Scope for v2

| In | Out (for now) | Why |
| --- | --- | --- |
| Booking requests and approval | Online payments and leases | Payments need a provider and compliance; v1's card-storage model was unsafe |
| Amenity bookings with conflict checks | Recurring bookings, calendars | One-off bookings cover the common case |
| Status history and notes | Notifications (email, push) | A desktop app has no delivery channel yet |
| One building network | Multiple buildings and campuses | The data model allows it; the UI doesn't need it yet |

## Success measures

- **Time to first reply** on booking requests (agent responsiveness).
- **Maintenance turnaround** by trade, from submitted to completed (shown in Reports).
- **Urgent requests open** at any time (shown in Reports; should be near zero).
- **Occupancy**: share of listings leased.
