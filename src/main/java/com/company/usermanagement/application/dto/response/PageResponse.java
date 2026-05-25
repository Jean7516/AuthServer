package com.company.usermanagement.application.dto.response;

import java.util.List;

/**
 * DTO genérico de respuesta paginada.
 * Se usa para cualquier listado: usuarios, roles, logs de auditoría, etc.
 *
 * @param <T> tipo del contenido de la página
 */
public record PageResponse<T>(
    List<T>  content,
    int      page,
    int      size,
    long     totalElements,
    int      totalPages,
    boolean  first,
    boolean  last
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long total) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) total / size);
        return new PageResponse<>(
            content, page, size, total, totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }
}
