package eventHub.deepak.mapper;

import eventHub.deepak.dto.response.*;
import eventHub.deepak.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {
    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(booking.getId(),booking.getEvent().getId(),booking.getStatus(),booking.getTotalAmount(),
                booking.getExpiresAt(),booking.getCreatedAt(),booking.getItems().stream()
                .map(i->new BookingItemResponse(i.getId(),i.getSeat().getId(),i.getSeatIdentifierSnapshot(),i.getPriceAtBooking())).toList());
    }
}
