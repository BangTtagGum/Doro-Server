package com.example.DoroServer.global.auth;

import com.example.DoroServer.domain.user.repository.UserRepository;
import com.example.DoroServer.global.auth.dto.JoinReq;
import com.example.DoroServer.global.auth.dto.LoginReq;
import com.example.DoroServer.global.auth.dto.SendAuthNumReq;
import com.example.DoroServer.global.auth.dto.VerifyAuthNumReq;
import com.example.DoroServer.global.common.SuccessResponse;
import com.example.DoroServer.global.jwt.CustomUserDetailsService;
import com.example.DoroServer.global.jwt.JwtTokenProvider;
import com.example.DoroServer.global.jwt.RedisService;
import com.example.DoroServer.global.message.MessageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import java.time.Duration;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "인증 🔐")
@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisService redisService;

    @Operation(summary = "001_01", description = "회원가입")
    @PostMapping("/join")
    public SuccessResponse<String> join (@RequestBody @Valid JoinReq joinReq){
        /**
         * 인증 번호 저장
         * 전화번호 인증을 클릭하면 레디스에 인증번호을 저장하고
         * 인증번호 확인을 클릭하면 레디스에 저장한 인증번호와 비교해서
         * 맞으면 그 인증된 전화번호를 테이블에 저장하고
         * 회원가입을 할 때 RequestBody 값으로 들어온 phoneNumber가 인증된 전화번호 테이블에 존재하는
         * 전화번호인지 확인한다.
         */
        authService.checkAccount(joinReq.getAccount());
        authService.join(joinReq);
        return SuccessResponse.successResponse("회원가입 완료");
    }

    @Operation(summary = "001_02", description = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody @Valid LoginReq loginReq){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginReq.getAccount(), loginReq.getPassword());
        log.info("AuthenticationToken.getName={}", authenticationToken.getName());
        log.info("AuthenticationToken.getCredentials={}", authenticationToken.getCredentials());
        log.info("AuthenticationToken={}", authenticationToken);
        String accessToken = createAccessToken(authenticationToken);
        String refreshToken = createRefreshToken(authenticationToken);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);

        redisService.setValues(loginReq.getAccount(), refreshToken, Duration.ofDays(180));

        return ResponseEntity.ok()
            .headers(httpHeaders).build();
    }

    @Operation(summary = "001_", description = "인증번호 전송")
    @PostMapping("/message/send")
    public SuccessResponse<String> sendAuthNum(@RequestBody @Valid SendAuthNumReq sendAuthNumReq){
        messageService.sendAuthNum(sendAuthNumReq);
        return SuccessResponse.successResponse("인증번호가 전송되었습니다.");
    }

    @Operation(summary = "001_", description = "인증번호 확인")
    @PostMapping("/message/verify")
    public SuccessResponse<String> verifyAuthNum(@RequestBody @Valid VerifyAuthNumReq verifyAuthNumReq){
        messageService.verifyAuthNum(verifyAuthNumReq);
        return SuccessResponse.successResponse("인증 성공");
    }

    @Operation(summary = "001_", description = "아이디 중복체크")
    @GetMapping("/check/account")
    public SuccessResponse<String> checkAccount(@RequestParam String account){
        authService.checkAccount(account);
        return SuccessResponse.successResponse("사용 가능한 아이디입니다.");
    }
    private String createAccessToken(UsernamePasswordAuthenticationToken authenticationToken) {
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("AccessToken 생성 준비 끝");
        return tokenProvider.createAccessToken(authentication.getName(), authentication.getAuthorities());
    }
    private String createRefreshToken(UsernamePasswordAuthenticationToken authenticationToken) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(authenticationToken.getName());
        return tokenProvider.createRefreshToken(userDetails.getUsername(), userDetails.getAuthorities());
    }


}
