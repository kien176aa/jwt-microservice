package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonResponse<T> implements Serializable {
        private T data;
    private Integer statusCode;
    private String message;
    public CommonResponse(T data, Integer statusCode, String message) {
        this.data = data;
        this.statusCode = statusCode;
        this.message = message;
    }
    public static <T> CommonResponse<T> ok() {
        return new CommonResponse<>(null, HttpStatus.OK.value(), "success");
    }

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(data, HttpStatus.OK.value(), "success");
    }

    public static <T> CommonResponse<T> notOk(T data) {
        return new CommonResponse<>(data, HttpStatus.BAD_REQUEST.value(), "error");
    }
}
