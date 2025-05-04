package com.leogouchon.squashapp.service;

import com.leogouchon.squashapp.dto.AuthenticateRequestDTO;
import com.leogouchon.squashapp.dto.AuthenticateResponseDTO;
import com.leogouchon.squashapp.model.RefreshToken;
import com.leogouchon.squashapp.model.Users;
import com.leogouchon.squashapp.repository.RefreshTokenRepository;
import com.leogouchon.squashapp.service.interfaces.IAuthenticateService;
import com.leogouchon.squashapp.service.interfaces.IUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticateService implements IAuthenticateService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    private final IUserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthenticateService(IUserService userService, RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
    }

    @Override
    public AuthenticateResponseDTO login(AuthenticateRequestDTO authRequest) throws AuthenticationException {
        Users user = userService.getUserByEmail(authRequest.getEmail());

        if (user == null || !user.getPassword().equals(authRequest.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateAndSaveRefreshToken(user);

        return new AuthenticateResponseDTO(accessToken, refreshToken);
    }

    @Override
    public String generateAccessToken(Users user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(user.getEmail())
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

        refreshTokenRepository.save(token);

        return refreshToken;
    }

    @Override
    public AuthenticateResponseDTO signIn(Users newUser) throws AuthenticationException {
        Users user = userService.createUser(newUser);
        return login(new AuthenticateRequestDTO(user.getEmail(), user.getPassword()));
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

        String email = claims.getSubject();
        return userService.getUserByEmail(email);
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
}
