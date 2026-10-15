package com.turbo.tracker.dto.response;

import java.util.List;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
		int pages = size == 0 ? 0 : (int) Math.ceil(totalElements / (double) size);
		return new PageResponse<>(content, page, size, totalElements, pages);
	}

}
