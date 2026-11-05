package eventHub.deepak.dto.response;
import java.math.BigDecimal;
public record SeatResponse(Long id,String identifier,Long screenId,Long categoryId,String categoryName,BigDecimal price) { }
