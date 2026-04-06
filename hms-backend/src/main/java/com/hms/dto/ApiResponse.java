package com.hms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private OffsetDateTime timestamp;
    private int status;
    private String message;
    private T data;
    private Object error;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(OffsetDateTime.now())
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(OffsetDateTime.now())
                .status(201)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, Object error) {
        return ApiResponse.<T>builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .message(message)
                .error(error)
                .build();
    }
}
