package com.newbie.auth.member.dto;

import com.newbie.auth.member.domain.Platform;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberDto {
    private Long memberId;
    private String email;
    private String nickname;
    private String address;
    private Platform platform;
    private LocalDate createTime;
    private LocalDate resignTime;
    private Integer isResigned;
}
