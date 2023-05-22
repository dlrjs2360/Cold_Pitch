package com.ColdPitch.domain.apicontroller;

import com.ColdPitch.domain.entity.dto.jwt.TokenDto;
import com.ColdPitch.domain.entity.dto.user.LoginDto;
import com.ColdPitch.domain.entity.dto.user.UserRequestDto;
import com.ColdPitch.domain.entity.dto.user.UserResponseDto;
import com.ColdPitch.domain.service.UserService;
import com.ColdPitch.utils.RandomUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/user")
public class UserApiController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "유저 전체 조회 ( 이후에 ADMIN 권한으로 열기)")
    public ResponseEntity<List<UserResponseDto>> findAllUser() {
        return ResponseEntity.status(200).body(userService.findAllUser());
    }


    @GetMapping("/{nickname}")
    @Operation(summary = "특정 유저 조회")
    public ResponseEntity<UserResponseDto> findMyProfile(@PathVariable("nickname") String nickname) {
        return ResponseEntity.status(200).body(userService.findByNickName(nickname));
    }

    @PatchMapping
    @Operation(summary = "유저 수정", description = "유저 이름, 전화번호, 닉네임, password 변경 기능")
    public ResponseEntity<UserResponseDto> updateProfile(@ApiIgnore Authentication authentication, @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(200).body(userService.updateProfile(authentication.getName(), userRequestDto));
    }

    @GetMapping("/dummy")
    @Operation(summary = "USER 더미데이터 생성", description = "default USER, 원하는 경우 ADMIN 입력")
    public ResponseEntity<List<UserResponseDto>> makeDummyUser(@RequestParam(defaultValue = "USER", name = "userType(Default : USER)") String userType, @RequestParam(defaultValue = "5", name = "size(Default : 5)") int size) {
        List<UserResponseDto> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            long rand = RandomUtil.getRandom(Integer.MAX_VALUE);
            list.add(userService.signup(new UserRequestDto("nick" + rand, "name" + rand, "password" + rand, "eamil" + rand, "phone" + rand, userType)));
        }
        return ResponseEntity.status(200).body(list);
    }


    @GetMapping("/testUserToken")
    @Operation(summary = "USER 테스트 토큰 생성")
    public ResponseEntity<String> makeDummyUserToken() {
        UserRequestDto urd = new UserRequestDto("testNick", "testName", "testPass", "testEamil@naver.com", "010-1234-1234", "USER");
        userService.deleteByEmail(urd.getEmail());
        userService.signup(urd);
        TokenDto login = userService.login(new LoginDto(urd.getEmail(), urd.getPassword()));
        return ResponseEntity.status(200).body("Bearer " + login.getAccessToken());
    }

    @GetMapping("/testAdminToken")
    @Operation(summary = "ADMIN 테스트 토큰 생성")
    public ResponseEntity<String> makeDummyAdminToken() {
        UserRequestDto urd = new UserRequestDto("testANick", "testAName", "testAPass", "testAEamil@naver.com", "010-5678-5678", "ADMIN");
        userService.deleteByEmail(urd.getEmail());
        userService.signup(urd);
        TokenDto login = userService.login(new LoginDto(urd.getEmail(), urd.getPassword()));
        return ResponseEntity.status(200).body("Bearer " + login.getAccessToken());
    }


}