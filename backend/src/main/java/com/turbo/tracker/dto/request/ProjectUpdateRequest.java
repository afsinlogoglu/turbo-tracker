package com.turbo.tracker.dto.request;

import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
		@Size(max = 80) String name,
		@Size(max = 2000) String description) {
}
