package com.javatechie.filter;

import com.javatechie.exception.UnAuthException;
import com.javatechie.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ConstantValue;
import org.example.constants.ErrorMessage;
import org.example.dtos.CheckPermissionRequest;
import org.example.dtos.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
//            if (validator.isSecured.test(exchange.getRequest())) {
//                //header contains token or not
//                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
//                    throw new UnAuthException("missing authorization header");
//                }
            if (!validator.isSecured.test(exchange.getRequest())) {
                return chain.filter(exchange);
            }


            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION) == null
                        ? null : exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
//                    jwtUtil.validateToken(authHeader);
                    log.info("validate start: {}", exchange.getRequest().getURI().getPath());
                    CommonResponse<?> response = restTemplate.postForObject(ConstantValue.URL_VALIDATE_TOKEN,
                            new CheckPermissionRequest(
                            authHeader,
                            exchange.getRequest().getURI().getPath()
                    ), CommonResponse.class);
                    log.info("validate end: {}", response);
                    if(response == null || 200 != response.getStatusCode()){
                        throw new UnAuthException(ErrorMessage.UN_AUTH);
                    }
                } catch (Exception e) {
                    log.error("invalid access...! {}", e.getMessage(), e);
                    throw new UnAuthException(ErrorMessage.UN_AUTH);
                }
//            }
            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
