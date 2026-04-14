package com.learnflow.backend.controllers;

import com.learnflow.backend.dto.AuthDtos;
import com.learnflow.backend.models.Role;
import com.learnflow.backend.models.User;
import com.learnflow.backend.repositories.UserRepository;
import com.learnflow.backend.security.JwtUtils;
import com.learnflow.backend.services.CourseTrackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    CourseTrackService courseTrackService;

    @PostMapping("/signin")
    public ResponseEntity<AuthDtos.AuthResponse> authenticateUser(@Valid @RequestBody AuthDtos.SignInRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();

        return ResponseEntity.ok(new AuthDtos.AuthResponse(jwt, courseTrackService.toSessionUser(userDetails)));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthDtos.AuthResponse> registerUser(@Valid @RequestBody AuthDtos.SignUpRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.email()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User(
            signUpRequest.fullName(),
            signUpRequest.email(),
            encoder.encode(signUpRequest.password()),
            Role.USER
        );

        userRepository.save(user);
        courseTrackService.autoEnrollStarterCourses(user);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(signUpRequest.email(), signUpRequest.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AuthDtos.AuthResponse(jwt, courseTrackService.toSessionUser((User) authentication.getPrincipal())));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDtos.SessionUser> currentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(courseTrackService.toSessionUser(user));
    }
}
