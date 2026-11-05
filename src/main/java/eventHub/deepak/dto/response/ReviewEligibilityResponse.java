package eventHub.deepak.dto.response;

public record ReviewEligibilityResponse(boolean eligible, String reason, ReviewResponse review) { }
