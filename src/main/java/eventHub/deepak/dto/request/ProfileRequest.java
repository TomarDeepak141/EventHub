package eventHub.deepak.dto.request;
import jakarta.validation.constraints.NotBlank;import jakarta.validation.constraints.Size;
public record ProfileRequest(@NotBlank @Size(max=120) String name) { }
