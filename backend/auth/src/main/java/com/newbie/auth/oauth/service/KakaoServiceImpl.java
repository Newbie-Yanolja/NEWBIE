package com.newbie.auth.oauth.service;

import com.newbie.auth.member.domain.Member;
import com.newbie.auth.member.dto.MemberDto;
import com.newbie.auth.member.repository.MemberRepository;
import com.newbie.auth.oauth.dto.KakaoMemberResponseDto;
import com.newbie.auth.oauth.exception.AlreadySignUpException;
import com.newbie.auth.global.security.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KakaoService {

    @Value("${kakao.grant-type}")
    private String grantType;
    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;

    @Override
    public KakaoMemberResponseDto getKakaoUser(String code) {
        String kakaoAccessToken = getAccessToken(code);
        KakaoMemberResponseDto kakaoMemberResponseDto = getKakaoUserInfo(kakaoAccessToken);

        //이미 가입한 유저라면
        Optional<Member> member = memberRepository.findByEmail(kakaoMemberResponseDto.getEmail());
        if (member.isPresent()) {
            MemberDto memberDto = mapper.map(member.get(), MemberDto.class);
            String token = jwtUtil.createAccessToken(memberDto);
            throw new AlreadySignUpException(token);
        }
        return kakaoMemberResponseDto;
    }

    private KakaoMemberResponseDto getKakaoUserInfo(String accessToken) {
        //HTTP 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        //Http 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoRequest = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            kakaoRequest,
            String.class
        );

        //Http 응답 (JSON)
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        KakaoMemberResponseDto kakaoMemberResponseDto = new KakaoMemberResponseDto();
        kakaoMemberResponseDto.setEmail(jsonNode.get("kakao_account").get("email").asText());
        return kakaoMemberResponseDto;
    }

    private String getAccessToken(String code) {
        //HTTP 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        //HTTP 바디 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        //Http 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoRequest = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            kakaoRequest,
            String.class
        );

        //Http 응답 (JSON)
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonNode.get("access_token").asText();
    }
}
