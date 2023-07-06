package com.ColdPitch.domain.service;

import com.ColdPitch.domain.entity.*;
import com.ColdPitch.domain.entity.dto.comment.CommentResponseDto;
import com.ColdPitch.domain.entity.dto.companyRegistraion.CompanyRegistrationDto;
import com.ColdPitch.domain.entity.dto.jwt.RefreshToken;
import com.ColdPitch.domain.entity.dto.jwt.TokenDto;
import com.ColdPitch.domain.entity.dto.jwt.TokenRequestDto;
import com.ColdPitch.domain.entity.dto.post.PostResponseDto;
import com.ColdPitch.domain.entity.dto.user.CompanyRequestDto;
import com.ColdPitch.domain.entity.dto.user.LoginDto;
import com.ColdPitch.domain.entity.dto.user.UserRequestDto;
import com.ColdPitch.domain.entity.dto.user.UserResponseDto;
import com.ColdPitch.domain.entity.user.CurState;
import com.ColdPitch.domain.entity.user.UserType;
import com.ColdPitch.domain.repository.*;
import com.ColdPitch.exception.CustomException;
import com.ColdPitch.jwt.TokenProvider;
import com.ColdPitch.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ColdPitch.exception.handler.ErrorCode.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CompanyRegistrationService companyRegistrationService;
    private final CommentService commentService;
    private final PostService postService;
    private final LikeRepository likeRepository;
    private final DislikeRepository dislikeRepository;
    private final PostRepository postRepository;

    @Transactional
    public UserResponseDto signUpUser(UserRequestDto userRoleDto) {
        //TODO 유저 이메일, 닉네임 중복 확인 ( 이메일 형식, 전화번호 형식 확인 부분도 추가해야함)
        User user = makeUser(userRoleDto);
        return UserResponseDto.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto signUpCompany(CompanyRequestDto companyRequestDto) {
        //TODO 유저 이메일, 닉네임 중복 확인 ( 이메일 형식, 전화번호 형식 확인 부분도 추가해야함)
        UserRequestDto userRequestDto = companyRequestDto.getUserRequestDto();
        User user = userRepository.save(makeUser(userRequestDto));

        //실제 존재하는 기업 회원인지 검증하는 로직
        CompanyRegistrationDto dto = companyRequestDto.getCompanyRegistrationDto();
        CompanyRegistration companyRegistration = companyRegistrationService.validateAndSaveCompanyRegistration(dto, user);
        user.registerCompany(companyRegistration);
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    public TokenDto login(LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authenticationToken = loginDto.toAuthentication();
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        //RefreshToken 저장
        RefreshToken refreshToken = new RefreshToken(authentication.getName(), tokenDto.getRefreshToken());
        refreshTokenRepository.save(refreshToken);
        return tokenDto;
    }

    @Transactional
    public void logout(String nowLoginEmail) {
        refreshTokenRepository.deleteByKey(nowLoginEmail);
    }

    @Transactional
    public UserResponseDto updateProfile(@ApiIgnore String userEmail, UserRequestDto userRequestDto) {
        //TODO 수정시에 validation 확인 ( 로그인한 사람이 본인이 맞는지 확인 )
        User user = userRepository.findOneWithAuthoritiesByEmail(userEmail).orElseThrow();
        user.updateProfile(userRequestDto);
        user.updatePassword(passwordEncoder.encode(userRequestDto.getPassword()));
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    public void deleteUser(String email) {
        //TODO 수정시에 validation 확인 ( 로그인한 사람이 본인이 맞는지 확인 )
        List<User> users = userRepository.findUserByEmailIncludeDeletedUser(email).orElseThrow();
        if (!users.isEmpty() && users.get(0).getCurState() == CurState.DELETED) {
            throw new CustomException(USER_ALREADY_WITHDRAWN);
        }
        logout(email); //리프레시 토큰을 삭제한다.
        userRepository.deleteByEmail(email);
    }

    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new CustomException(USER_INVALID_REFRESH_TOKEN);
        }

        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName()).orElseThrow(() -> new CustomException(USER_NOT_ACTIVE));

        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new CustomException(USER_INVALID_USER_REFRESH_TOKEN);
        }

        //새로운 토큰 발급
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);
        return tokenDto;
    }


    //조회
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public UserResponseDto findByNickName(String nickname) {
        return UserResponseDto.fromEntity(userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(USER_NICKNAME_NOT_FOUND)));
    }

    public List<UserResponseDto> findAllUser() {
        return userRepository.findAll().stream().map(UserResponseDto::fromEntity).collect(Collectors.toList());
    }

    public List<PostResponseDto> getEvaluatedPostsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email: " + email));
        Long userId = user.getId();

        List<Like> likes = likeRepository.findByUserId(userId).orElse(new ArrayList<>());
        List<Dislike> dislikes = dislikeRepository.findByUserId(userId).orElse(new ArrayList<>());
        List<CommentResponseDto> comments = commentService.findCommentsByUserId(userId);

        List<Post> posts = new ArrayList<>();

        for (Like like : likes) {
            posts.add(postRepository.findById(like.getPostId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid postId: " + like.getPostId())));
        }

        for (Dislike dislike : dislikes) {
            posts.add(postRepository.findById(dislike.getPostId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid postId: " + dislike.getPostId())));
        }

        for (CommentResponseDto comment : comments) {
            posts.add(postRepository.findById(comment.getPostId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid postId: " + comment.getPostId())));
        }

        List<PostResponseDto> postResponses = posts.stream()
                .map(post -> PostResponseDto.of(post, postService.getSelection(userId, post.getId())))
                .collect(Collectors.toList());

        // 게시글 작성 시간별로 내림차순으로 정렬 (최신글이 제일 위에 오도록)
        postResponses.sort(Comparator.comparing(PostResponseDto::getCreateAt).reversed());

        return postResponses;
    }

    public List<PostResponseDto> findMyWritePost(String email) {
        User user = userRepository.findOneWithAuthoritiesByEmail(email).orElseThrow();
        return user.getPosts().stream().map(o -> PostResponseDto.of(o, null)).collect(Collectors.toList());
    }

    //현재 시큐리티 컨텍스에 있는 유저정보와 권환 정보를 준다
    public Optional<User> getMemberWithAuthorities() {
        return SecurityUtil.getCurrentUserEmail().flatMap(userRepository::findOneWithAuthoritiesByEmail);
    }

    private User makeUser(UserRequestDto userRequestDto) {
        return User.builder()
                .name(userRequestDto.getName())
                .nickname(userRequestDto.getNickname())
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .email(userRequestDto.getEmail())
                .phoneNumber(userRequestDto.getPhoneNumber())
                .userType(UserType.of(userRequestDto.getUserType()))
                .curState(CurState.LIVE)
                .posts(new ArrayList<>())
                .userTags(new ArrayList<>())
                .companyRegistration(null)
                .nickname(userRequestDto.getNickname()).build();
    }
}
