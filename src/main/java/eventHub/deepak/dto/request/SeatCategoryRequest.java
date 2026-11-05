package eventHub.deepak.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record SeatCategoryRequest(@NotBlank @Size(max=80) String name,@NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal price) { }
