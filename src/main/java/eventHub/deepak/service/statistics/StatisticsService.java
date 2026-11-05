package eventHub.deepak.service.statistics;

import eventHub.deepak.dto.response.AdminStatisticsResponse;
import eventHub.deepak.dto.response.EventStatisticsResponse;
import eventHub.deepak.dto.response.OrganizerStatisticsResponse;
import eventHub.deepak.entity.Event;
import eventHub.deepak.entity.User;
import eventHub.deepak.enums.BookingStatus;
import eventHub.deepak.enums.Role;
import eventHub.deepak.enums.TicketStatus;
import eventHub.deepak.repository.BookingRepository;
import eventHub.deepak.repository.EventRepository;
import eventHub.deepak.repository.EventReviewRepository;
import eventHub.deepak.repository.FavouriteRepository;
import eventHub.deepak.repository.TicketRepository;
import eventHub.deepak.repository.UserRepository;
import eventHub.deepak.util.PageableSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StatisticsService {
    private final EventRepository events;
    private final BookingRepository bookings;
    private final TicketRepository tickets;
    private final EventReviewRepository reviews;
    private final FavouriteRepository favourites;
    private final UserRepository users;

    public StatisticsService(EventRepository events, BookingRepository bookings, TicketRepository tickets,
                             EventReviewRepository reviews, FavouriteRepository favourites, UserRepository users) {
        this.events=events; this.bookings=bookings; this.tickets=tickets; this.reviews=reviews; this.favourites=favourites; this.users=users;
    }

    @Transactional(readOnly = true)
    public OrganizerStatisticsResponse organizer(String email, Pageable pageable) {
        User actor=activeUser(email,Role.ORGANIZER);
        Pageable safe=PageableSupport.validate(pageable,Set.of("id","name","startTime","eventStatus","createdAt"),Sort.by(Sort.Order.asc("startTime"),Sort.Order.asc("id")));
        Page<Event> eventPage=events.findByOrganizerUserId(actor.getId(),safe);
        Map<Long,BookingAggregate> bookingMap=bookings(bookingRows(actor.getId()));
        Map<Long,Long> ticketMap=countMap(tickets.aggregateCountByOrganizer(actor.getId()));
        Map<Long,ReviewAggregate> reviewMap=reviewMap(reviews.aggregateByOrganizer(actor.getId()));
        Page<EventStatisticsResponse> perEvent=eventPage.map(event->{
            BookingAggregate b=bookingMap.getOrDefault(event.getId(),new BookingAggregate(0,BigDecimal.ZERO));
            ReviewAggregate r=reviewMap.getOrDefault(event.getId(),new ReviewAggregate(0,0));
            return new EventStatisticsResponse(event.getId(),event.getName(),event.getEventStatus(),b.count,b.value,
                    ticketMap.getOrDefault(event.getId(),0L),r.count,r.average);
        });
        long bookingCount=bookingMap.values().stream().mapToLong(a->a.count).sum();
        BigDecimal bookingValue=bookingMap.values().stream().map(a->a.value).reduce(BigDecimal.ZERO,BigDecimal::add);
        long ticketCount=ticketMap.values().stream().mapToLong(Long::longValue).sum();
        long reviewCount=reviewMap.values().stream().mapToLong(r->r.count).sum();
        double rating=reviewCount==0?0:reviewMap.values().stream().mapToDouble(r->r.average*r.count).sum()/reviewCount;
        return new OrganizerStatisticsResponse(eventPage.getTotalElements(),bookingCount,bookingValue,ticketCount,reviewCount,rating,perEvent);
    }

    @Transactional(readOnly = true)
    public AdminStatisticsResponse admin(String email) {
        activeUser(email,Role.ADMIN);
        return new AdminStatisticsResponse(users.countByIsActiveTrue(),users.countByRole(Role.CUSTOMER),users.countByRole(Role.ORGANIZER),users.countByRole(Role.ADMIN),
                events.count(),bookings.countByStatus(BookingStatus.CONFIRMED),bookings.sumByStatus(BookingStatus.CONFIRMED),
                tickets.countByStatus(TicketStatus.ACTIVE),tickets.countByStatus(TicketStatus.USED),tickets.countByStatus(TicketStatus.CANCELLED),tickets.countByStatus(TicketStatus.EXPIRED),
                reviews.count(),reviews.averageRating(),favourites.count());
    }

    private User activeUser(String email,Role required) {
        User actor=users.findByEmail(email).orElseThrow(()->new AccessDeniedException("Account is unavailable"));
        if(!Boolean.TRUE.equals(actor.getIsActive())||actor.getRole()!=required)
            throw new AccessDeniedException("An active "+required.name().toLowerCase()+" account is required");
        return actor;
    }
    private List<Object[]> bookingRows(Long organizerId) { return bookings.aggregateByOrganizerAndStatus(organizerId,BookingStatus.CONFIRMED); }
    private Map<Long,BookingAggregate> bookings(List<Object[]> rows) {
        Map<Long,BookingAggregate> values=new HashMap<>();
        for(Object[] row:rows)values.put(number(row[0]),new BookingAggregate(number(row[1]),decimal(row[2])));
        return values;
    }
    private Map<Long,Long> countMap(List<Object[]> rows) {
        Map<Long,Long> values=new HashMap<>(); for(Object[] row:rows)values.put(number(row[0]),number(row[1])); return values;
    }
    private Map<Long,ReviewAggregate> reviewMap(List<Object[]> rows) {
        Map<Long,ReviewAggregate> values=new HashMap<>(); for(Object[] row:rows)values.put(number(row[0]),new ReviewAggregate(number(row[1]),((Number)row[2]).doubleValue())); return values;
    }
    private long number(Object value){return value==null?0:((Number)value).longValue();}
    private BigDecimal decimal(Object value){return value==null?BigDecimal.ZERO:value instanceof BigDecimal amount?amount:new BigDecimal(value.toString());}
    private record BookingAggregate(long count,BigDecimal value) { }
    private record ReviewAggregate(long count,double average) { }
}
