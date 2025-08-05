package com.leogouchon.hubscore.authenticate_service.service.impl;

import com.leogouchon.hubscore.authenticate_service.dto.AuthenticateRequestDTO;
import com.leogouchon.hubscore.authenticate_service.dto.DoubleTokenDTO;
import com.leogouchon.hubscore.authenticate_service.entity.InvitationToken;
import com.leogouchon.hubscore.authenticate_service.entity.RefreshToken;
import com.leogouchon.hubscore.user_service.entity.Users;
import com.leogouchon.hubscore.authenticate_service.repository.InvitationTokenRepository;
import com.leogouchon.hubscore.authenticate_service.repository.RefreshTokenRepository;
import com.leogouchon.hubscore.user_service.repository.UserRepository;
import com.leogouchon.hubscore.authenticate_service.service.AuthenticateService;
import com.leogouchon.hubscore.user_service.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticateServiceDefault implements AuthenticateService {
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    @Autowired
    public AuthenticateServiceDefault(
            UserService userService,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            UserRepository usersRepository,
            InvitationTokenRepository invitationTokenRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = usersRepository;
        this.invitationTokenRepository = invitationTokenRepository;
    }

    @Override
    public DoubleTokenDTO login(AuthenticateRequestDTO authRequest) throws AuthenticationException {
        Users user = userService.getUserByEmail(authRequest.getEmail());

        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateAndSaveRefreshToken(user);

        return new DoubleTokenDTO(accessToken, refreshToken);
    }

    @Override
    public String generateAccessToken(Users user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId())) // ID en tant que subject
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAndSaveRefreshToken(Users user) {
        String refreshToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusDays(30);

        RefreshToken token = new RefreshToken();
        token.setToken(refreshToken);
        token.setUser(user);
        token.setExpiryDate(expiry);
        token.setRevoked(false);

        refreshTokenRepository.revokeAllValidTokensByUser(user);
        refreshTokenRepository.save(token);

        return refreshToken;
    }

    @Override
    public DoubleTokenDTO signUp(String email, String password, String invitationToken) throws AuthenticationException {
        InvitationToken invitation = invitationTokenRepository.findByToken(invitationToken);

        if (invitation == null) throw new IllegalArgumentException("Invitation token not found");
        if (Boolean.TRUE.equals(invitation.getIsUsed()) || invitation.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired or already used");
        }
        Users user = userService.createUser(new Users(email, password, invitation.getPlayer()));

        return login(new AuthenticateRequestDTO(user.getEmail(), password));
    }

    @Override
    public boolean isValidToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Users getUserFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = claims.getSubject();
        return userService.getUserById(UUID.fromString(userId));
    }

    @Override
    @Transactional
    public void logout(String currentRefreshToken) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(currentRefreshToken);
        refreshToken.ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Override
    public String refreshAccessToken(String refreshToken) throws AuthenticationException {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        if (token.isRevoked() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("Refresh token expired or revoked");
        }

        Users user = token.getUser();
        return generateAccessToken(user);
    }

    @Override
    public Users getCurrentUser(String accessToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        UUID userId = UUID.fromString(claims.getSubject());

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public boolean isUserAdmin(String accessToken) {
        Users user = getCurrentUser(accessToken);
        return Boolean.TRUE.equals(user.getIsAdmin());
    }

    @Override
    @Scheduled(cron = "0 0 3 1 * ?")
    public void cleanupRevokedRefreshTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(15);
        int deletedCount = refreshTokenRepository.deleteRovokedTokensOlderThan(cutoffDate);
        System.out.println("Deleted " + deletedCount + " revoked refresh tokens older than " + cutoffDate);
    }
}
