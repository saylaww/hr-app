package uz.pdp.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.demo.dto.LoginDto;
import uz.pdp.demo.dto.PasswordDto;
import uz.pdp.demo.dto.RegisterDto;
import uz.pdp.demo.entity.Role;
import uz.pdp.demo.entity.User;
import uz.pdp.demo.entity.enums.RoleName;
import uz.pdp.demo.model.Response;
import uz.pdp.demo.repository.RoleRepository;
import uz.pdp.demo.repository.UserRepository;
import uz.pdp.demo.security.JwtProvider;

import java.util.*;

@Service
public class AuthService implements UserDetailsService {

    final
    UserRepository userRepository;
    final
    PasswordEncoder passwordEncoder;
    final
    RoleRepository roleRepository;
    final
    JavaMailSender javaMailSender;
    final
    AuthenticationManager authenticationManager;
    final
    JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, JavaMailSender javaMailSender, AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.javaMailSender = javaMailSender;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    public Response register(RegisterDto registerDto) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal().equals("anonymousUser")) {
            final boolean existsByEmail = userRepository.existsByEmail(registerDto.getEmail());
            if (existsByEmail) {
                return new Response("Email already exists", false);
            }
            final User user = saveTempUser(registerDto);
            sendEmail(user.getEmail(), user.getEmailCode());
            userRepository.save(user);
            return new Response("Confirmation code sent to email address", true);
        } else {
            final User user = (User) authentication.getPrincipal();
            final Set<Role> roles = user.getRoles();
            int roleId = 0;
            for (Role role : roles) {
                if (role.getName().equals(RoleName.DIRECTOR)) {
                    roleId = 1;
                } else if (role.getName().equals(RoleName.MANAGER)) {
                    roleId = 2;
                } else if (role.getName().equals(RoleName.HR_MANAGER)) {
                    roleId = 3;
                } else if (role.getName().equals(RoleName.EMPLOYEE)) {
                    roleId = 4;
                }
            }

            if (roleId == 1) {
                final Set<Integer> rolesId = registerDto.getRolesId();
                for (Integer integer : rolesId) {
                    if (integer == 1) {
                        return new Response("You are not allowed to add Director", false);
                    } else if (integer == 2) {
                        final User user1 = userRepository.save(saveTempUser(registerDto));
                        sendEmail(user1.getEmail(), user1.getEmailCode());
                        return new Response("Manager added!", true);
                    } else if (integer == 3) {
                        final User user1 = userRepository.save(saveTempUser(registerDto));
                        sendEmail(user1.getEmail(), user1.getEmailCode());
                        return new Response("HR Manager added!", true);
                    } else {
                        return new Response("", false);
                    }
                }
            } else if (roleId == 2) {
                final Set<Integer> rolesId = registerDto.getRolesId();
                for (Integer integer : rolesId) {
                    if (integer == 1) {
                        return new Response("You are not allowed to add Director", false);
                    } else if (integer == 2) {
                        final User user1 = userRepository.save(saveTempUser(registerDto));
                        return new Response("You are not allowed to add Manager", false);
                    } else if (integer == 3) {
                        final User user1 = userRepository.save(saveTempUser(registerDto));
                        return new Response("Hr added", true);
                    } else if (integer == 4) {
                        return new Response("You are not allowed to add Emlployee", false);
                    }
                }
            } else if (roleId == 3) {
                final Set<Integer> rolesId = registerDto.getRolesId();
                for (Integer integer : rolesId) {
                    if (integer == 1) {
                        return new Response("You are not allowed to add Director", false);
                    } else if (integer == 2) {
                        final User user1 = userRepository.save(saveTempUser(registerDto));
                        return new Response("You are not allowed to add Manager", false);
                    } else if (integer == 3) {
                        return new Response("You are not allowed to add Hr", false);
                    } else if (integer == 4) {
                        final User user1 = userRepository.save(saveTempUser(registerDto));
                        sendEmailForEmployees(user1.getEmail(), user1.getEmailCode());
                        return new Response("Employee added!", true);
                    }
                }
            } else {
                return new Response("You are not have an empowerment", false);
            }
        }
        return null;
    }

    public User saveTempUser(RegisterDto registerDto) {

        User user = new User();
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();

        for (Integer integer : registerDto.getRolesId()) {
            final Optional<Role> optionalRole = roleRepository.findById(integer);
            if (optionalRole.isEmpty()) {
                return null;
            }
            final Role role = optionalRole.get();
            roles.add(role);
        }
        user.setRoles(roles);
        user.setEmailCode(UUID.randomUUID().toString());
        user.setSalary(registerDto.getSalary());
        return user;
    }

    public void sendEmail(String sendingEmail, String emailCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@gmail.com");
        message.setTo(sendingEmail);
        message.setSubject("Confirmation Account");
        message.setText("http://localhost:8081/api/auth/verifyEmail?emailCode=" + emailCode
                + "&email=" + sendingEmail);
        javaMailSender.send(message);
    }

    public void sendEmailForEmployees(String sendingEmail, String emailCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@gmail.com");
        message.setTo(sendingEmail);
        message.setSubject("Confirmation Account");
        message.setText("http://localhost:8081/api/auth/verifyEmailForEmployees?emailCode=" + emailCode
                + "&email=" + sendingEmail);
        javaMailSender.send(message);
    }

    public Response verifyEmailForEmployees(String email, String emailCode, PasswordDto passwordDto) {
        final Optional<User> optionalUser = userRepository.findByEmailAndEmailCode(email, emailCode);
        if (optionalUser.isEmpty()) {
            return new Response("Email already verified!", false);
        }
        final User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
        user.setEnabled(true);
        user.setEmailCode(null);
        userRepository.save(user);
        return new Response("Verified", true);
    }

    public Response verifyEmail(String email, String emailCode) {

        final Optional<User> optionalUser = userRepository.findByEmailAndEmailCode(email, emailCode);
        if (optionalUser.isEmpty()) {
            return new Response("Email already verified!", false);
        }
        final User user = optionalUser.get();
        user.setEnabled(true);
        user.setEmailCode(null);
        userRepository.save(user);
        return new Response("Verified", true);
    }

    public Response login(LoginDto loginDto) {
        try {
            final Authentication authenticate = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(),
                            loginDto.getPassword()));

            final User user = (User) authenticate.getPrincipal();
            final String token = jwtProvider.generateToken(loginDto.getUsername(), user.getRoles());

            return new Response("Token", true, token);
        } catch (BadCredentialsException e) {
            return new Response("Login or password incorrect", true);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        final Optional<User> optionalUser = userRepository.findByEmail(s);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(s + " not found");
        }
        return optionalUser.get();
    }
}
