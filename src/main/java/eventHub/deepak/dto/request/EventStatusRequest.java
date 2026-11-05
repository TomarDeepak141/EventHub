package eventHub.deepak.dto.request;
import eventHub.deepak.enums.EventStatus;import jakarta.validation.constraints.NotNull;
public record EventStatusRequest(@NotNull EventStatus status) { }
