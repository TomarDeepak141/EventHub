package eventHub.deepak.dto.request;

import eventHub.deepak.enums.SandboxOutcome;
import jakarta.validation.constraints.NotNull;
public record SandboxCompletionRequest(@NotNull SandboxOutcome outcome) { }
