package com.javatechie.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "confirmPassword", "token", "authorization", "file", "image", "accessToken", "refreshToken"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info(">> REQUEST [{} {}] Headers: {}", request.getMethod(), request.getURI(), request.getHeaders());

        // Only read body if it’s a POST/PUT and content type is JSON
        if (requiresBodyLog(request)) {
            return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        String rawBody = new String(bytes, StandardCharsets.UTF_8);
                        String sanitizedBody = sanitizeJsonBody(rawBody);

                        log.info(">> REQUEST BODY: {}", sanitizedBody);

                        // rebuild request body for downstream services
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("X-Request-Logged", "true")
                                .build();

                        return chain.filter(exchange.mutate()
                                .request(new ServerHttpRequestDecorator(mutatedRequest) {
                                    @Override
                                    public Flux<DataBuffer> getBody() {
                                        return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                                    }
                                })
                                .response(decorateResponse(exchange, request.getMethod(), request.getURI())).build());
                    });
        }

        return chain.filter(exchange);
    }

    private ServerHttpResponse decorateResponse(ServerWebExchange exchange, HttpMethod method, URI uri) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        return new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (getStatusCode() != null && getStatusCode().is2xxSuccessful()) {
                    Flux<? extends DataBuffer> flux = Flux.from(body);
                    return super.writeWith(
                            flux.buffer().map(dataBuffers -> {
                                DataBuffer joined = bufferFactory.join(dataBuffers);
                                byte[] content = new byte[joined.readableByteCount()];
                                joined.read(content);
                                DataBufferUtils.release(joined);

                                String responseBody = new String(content, StandardCharsets.UTF_8);
                                logResponseBody(responseBody, method, uri);

                                return bufferFactory.wrap(content);
                            })
                    );
                }
                return super.writeWith(body);
            }
        };
    }

    private void logResponseBody(String body, HttpMethod method, URI uri) {
        if (body.length() > 5000) {
            log.info("<< RESPONSE BODY [{} {}]: [too large to display]", method, uri);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(body);

            if (json.isObject()) {
                ObjectNode sanitized = (ObjectNode) json;
                SENSITIVE_FIELDS.forEach(field -> {
                    if (sanitized.has(field)) {
                        sanitized.put(field, "***");
                    }
                });
                body = mapper.writeValueAsString(sanitized);
            }
        } catch (Exception e) {
            // ignore parse errors for response
        }

        log.info("<< RESPONSE BODY [{} {}]: {}", method, uri, body);
    }

    private boolean requiresBodyLog(ServerHttpRequest request) {
        HttpMethod method = request.getMethod();
        MediaType contentType = request.getHeaders().getContentType();
        return (method == HttpMethod.POST || method == HttpMethod.PUT)
                && contentType != null && contentType.includes(MediaType.APPLICATION_JSON);
    }

    private String sanitizeJsonBody(String rawBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(rawBody);

            if (json.isObject()) {
                ObjectNode sanitized = (ObjectNode) json;
                SENSITIVE_FIELDS.forEach(field -> {
                    if (sanitized.has(field)) {
                        sanitized.put(field, "***");
                    }
                });
                return mapper.writeValueAsString(sanitized);
            }
        } catch (Exception e) {
            log.warn("Could not parse body for logging: {}", e.getMessage());
        }
        return "[unreadable or binary body]";
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

