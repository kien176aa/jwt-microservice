//package com.javatechie.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//@EnableWebMvc
//public class CorsConfig implements WebMvcConfigurer {
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins("*") // Cho phép tất cả domain
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                        .allowedHeaders("*")
//                        .allowCredentials(false); // Nếu cần gửi cookie, đổi thành true
//            }
//        };
//    }
//}
