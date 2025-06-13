package com.javatechie.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    public static final String SECRET = "5svsdvsdvsdewfw4t34t2r292F423F4528482B4D6251655468576D5A71347437";


    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }



    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

//    public void checkPermissions(String token, String url) throws Exception {
//        if(url.startsWith("/auth/")){
//            return;
//        }
//        jwtService.validateToken(token);
//        User user = jwtService.getCurrentUser(token);
//        if (url.startsWith("/users/") && !user.getRole().contains("ADMIN")) {
//            throw new Exception("You do not have permission to access this resource");
//        }
//    }
}
