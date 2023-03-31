package com.example.DoroServer.global.jwt;

import com.example.DoroServer.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;


@Component
@Slf4j
@PropertySource("classpath:/jwt.properties")
public class JwtTokenProvider {
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private String secretKey;
    private final String refreshSecretKey;
    private final Integer accessTime;
    private final Integer refreshTime;

    public JwtTokenProvider(
            UserRepository userRepository,
            CustomUserDetailsService customUserDetailsService,
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.secret-refresh}") String refreshSecretKey,
            @Value("${jwt.access-token-seconds}") Integer accessTime,
            @Value("${jwt.refresh-token-seconds}") Integer refreshTime) {
        this.userRepository = userRepository;
        this.customUserDetailsService = customUserDetailsService;
        this.secretKey = secretKey;
        this.refreshSecretKey = refreshSecretKey;
        this.accessTime = accessTime;
        this.refreshTime = refreshTime;
    }
    // 시크릿키 Base64 인코딩
    @PostConstruct
    protected void init(){
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("type","jwt")
                //Payload에 Private Claim을 담기 위함
                .claim("userId",userId)
                //발급시간
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+accessTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
    // 토큰에서 인증 정보 가져오기 - 권한 처리를 위함
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    //토큰에서 회원 정보 추출
    private String getUserPk(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰의 유효성, 만료일자 확인
    public boolean validateToken(ServletRequest servletRequest, String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            servletRequest.setAttribute("exception","MalformedJwtException");
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            servletRequest.setAttribute("exception","ExpiredJwtException");
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            servletRequest.setAttribute("exception","UnsupportedJwtException");
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            servletRequest.setAttribute("exception","IllegalArgumentException");
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public String resolveToken(HttpServletRequest request){
        return request.getHeader("Authorization");
    }
}

