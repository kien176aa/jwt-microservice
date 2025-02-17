package com.javatechie.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CommonResponse;
import org.example.exception.RecordExistException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements WebExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse;
        if (ex instanceof UnAuthException) {
            log.error("UnAuthException!");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            errorResponse = new ErrorResponse(401, ex.getMessage());
        } else if(ex instanceof NotFoundException){
            log.error("Not found ex!");
            response.setStatusCode(HttpStatus.NOT_FOUND);
            errorResponse = new ErrorResponse(404, ex.getMessage());
        }else if(ex instanceof RecordExistException){
            log.error("Existed ex!");
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            errorResponse = new ErrorResponse(400, ex.getMessage());
        } else {
            log.error("Internal ex!");
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            errorResponse = new ErrorResponse(500, ex.getMessage());
        }

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(CommonResponse.notOk(errorResponse));
            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }
}
