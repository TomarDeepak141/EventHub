package eventHub.deepak.dto.request;
import eventHub.deepak.enums.Role;import jakarta.validation.constraints.NotNull;
public record UserAdminRequest(@NotNull Role role,@NotNull Boolean active) { }
