package eventHub.deepak.controller;

import eventHub.deepak.dto.response.ReservationResponse;
import eventHub.deepak.globalExceptionHandler.GlobalExceptionHandler;
import eventHub.deepak.service.impl.BookingService;
import eventHub.deepak.service.impl.SeatReservationService;
import eventHub.deepak.service.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerTest {
    @Test void derivesReservationCustomerFromAuthenticatedPrincipal() throws Exception {
        SeatReservationService reservationService=mock(SeatReservationService.class);
        BookingService bookingService=mock(BookingService.class);
        var auth=UsernamePasswordAuthenticationToken.authenticated("customer@example.test",null,List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(reservationService.reserve(42L,"customer@example.test",List.of(7L)))
                .thenReturn(new ReservationResponse("01234567-89ab-cdef-0123-456789abcdef",42L,List.of(7L),Instant.parse("2026-11-04T00:05:00Z")));
        MockMvc mvc=standaloneSetup(new BookingController(bookingService,reservationService,mock(PaymentService.class))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post("/events/42/reservations").principal(auth).contentType("application/json").content("{\"seatIds\":[7]}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.eventId").value(42)).andExpect(jsonPath("$.seatIds[0]").value(7));
        verify(reservationService).reserve(42L,"customer@example.test",List.of(7L));
    }
}
