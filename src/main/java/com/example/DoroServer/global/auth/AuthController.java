package com.example.DoroServer.global.auth;

import com.example.DoroServer.domain.user.repository.UserRepository;
import com.example.DoroServer.global.auth.dto.JoinReq;
import com.example.DoroServer.global.auth.dto.SendAuthNumReq;
import com.example.DoroServer.global.auth.dto.VerifyAuthNumReq;
import com.example.DoroServer.global.common.SuccessResponse;
import com.example.DoroServer.global.message.MessageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    /**
     * ReponseEntity 사용 - header에 Token 추가
     * Access 토큰은 헤더에 저장 Refresh 토큰은 Redis 저장
     */
    @Operation(summary = "001_01", description = "회원가입")
    public ResponseEntity<?> join (@RequestBody @Valid JoinReq joinReq){
        /**
         * 인증 번호 저장
         * 전화번호 인증을 클릭하면 레디스에 인증번호을 저장하고
         * 인증번호 확인을 클릭하면 레디스에 저장한 인증번호와 비교해서
         * 맞으면 그 인증된 전화번호를 테이블에 저장하고
         * 회원가입을 할 때 RequestBody 값으로 들어온 phoneNumber가 인증된 전화번호 테이블에 존재하는
         * 전화번호인지 확인한다.
         */

        return ResponseEntity.ok("회원가입 완료");
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
}
