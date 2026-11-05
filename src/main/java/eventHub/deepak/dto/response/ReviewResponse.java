package eventHub.deepak.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long eventId, String customerName, short rating, String comment,
                             LocalDateTime createdAt, LocalDateTime updatedAt) { }
