package uz.pdp.demo.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.demo.dto.LoginDto;
import uz.pdp.demo.dto.PasswordDto;
import uz.pdp.demo.dto.RegisterDto;
import uz.pdp.demo.model.Response;
import uz.pdp.demo.service.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public HttpEntity<?> login(@RequestBody LoginDto loginDto) {
        Response response = authService.login(loginDto);
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }

    @PostMapping("/register")
    public HttpEntity<?> register(@RequestBody RegisterDto registerDto) {
        final Response response = authService.register(registerDto);
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }

    @GetMapping("verifyEmailForEmployees")
    public HttpEntity<?> verifyEmail(@RequestParam String emailCode, @RequestParam String email, @RequestBody PasswordDto passwordDto) {
        Response response = authService.verifyEmailForEmployees(email, emailCode, passwordDto);
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }

    @GetMapping("verifyEmail")
    public HttpEntity<?> verifyEmail(@RequestParam String emailCode, @RequestParam String email) {
        Response response = authService.verifyEmail(email, emailCode);
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }
}
