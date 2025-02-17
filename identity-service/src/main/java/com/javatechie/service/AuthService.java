package com.javatechie.service;

import com.javatechie.dto.AuthRequest;
import com.javatechie.entity.UrlAccess;
import com.javatechie.entity.User;
import com.javatechie.exceptions.PermissionException;
import com.javatechie.repository.UserCredentialRepository;
import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ConstantValue;
import org.example.constants.ErrorMessage;
import org.example.dtos.CheckPermissionRequest;
import org.example.dtos.UserDto;
import org.example.exception.RecordExistException;
import org.example.exception.UnAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserCredentialRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UrlAccessService urlAccessService;

    public UserDto saveUser(UserDto credential) throws RecordExistException {
        User user = repository.findByEmail(credential.getEmail());
        if(user != null){
            throw new RecordExistException("User");
        }
        User saveUser = new User();
        saveUser.setName(credential.getName());
        saveUser.setEmail(credential.getEmail());
        saveUser.setRole("USER");
        saveUser.setStatus(true);
        saveUser.setPassword(passwordEncoder.encode(credential.getPassword()));
        repository.save(saveUser);
        credential.setId(saveUser.getId());
        return credential;
    }

    public String generateToken(String username) {
        return jwtService.generateToken(username);
    }

    public void validateToken(CheckPermissionRequest request) {
        List<UrlAccess> urlAccessList = urlAccessService.getAllUrlAccess();
        boolean noNeedToCheck = urlAccessList.stream()
                .anyMatch(access ->
                        ConstantValue.ALLOW_URL.contains(access.getRole()) &&
                                matchesUrl(request.getUrl(), access.getUrl()));
        if (noNeedToCheck) {
            log.info("noNeedToCheck {}", request.getUrl());
            return;
        }

        jwtService.validateToken(request.getToken());
        User user = jwtService.getCurrentUser(request.getToken());
        boolean hasPermission = urlAccessList.stream()
                .anyMatch(access ->
                        user.getRole().contains(access.getRole()) &&
                                matchesUrl(request.getUrl(), access.getUrl()));

        if (!hasPermission) {
            throw new PermissionException("You do not have permission to access this resource");
        }
    }

    private boolean matchesUrl(String requestUrl, String storedUrls) {
        String[] urls = storedUrls.split(",");
        for (String url : urls) {
            if (requestUrl.startsWith(url.trim())) {
                return true;
            }
        }
        return false;
    }



    public Object login(AuthRequest request) throws Exception {
        User user = repository.findByEmail(request.getEmail());
        log.info("User: {}", user);
        log.info("Request: {}", passwordEncoder.encode(request.getPassword()));
        if(user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new Exception(ErrorMessage.INVALID_EMAIL_PASSWORD);
        }
        return generateToken(request.getEmail());
    }

    public Object changeStatus(Long id) throws Exception {
        Optional<User> user = repository.findById(id);
        if(user.isEmpty()){
            throw new Exception(ErrorMessage.USER_NOT_FOUND);
        }
        user.get().setStatus(!user.get().getStatus());
        repository.save(user.get());
        return id;
    }

    public Object create(UserDto credential) {
        User user = repository.findByEmail(credential.getEmail());
        if(user != null){
            throw new RecordExistException("User");
        }
        User saveUser = new User();
        saveUser.setName(credential.getName());
        saveUser.setEmail(credential.getEmail());
        saveUser.setRole(credential.getRole());
        saveUser.setStatus(true);
        saveUser.setPassword(passwordEncoder.encode(credential.getPassword()));
        repository.save(saveUser);
        credential.setId(saveUser.getId());
        return credential;
    }
}
