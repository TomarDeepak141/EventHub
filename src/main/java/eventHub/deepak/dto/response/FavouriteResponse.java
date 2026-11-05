package eventHub.deepak.dto.response;

import java.time.LocalDateTime;

public record FavouriteResponse(Long id, Long eventId, EventResponse event, LocalDateTime createdAt) { }
