package com.newbie.auth.member.domain;


import com.newbie.auth.member.dto.MemberImageDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "member_image")
@Builder
@DynamicInsert
@DynamicUpdate
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberImage {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(optional = false, fetch = LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "member_image")
    private String memberImage;


    public void updateMemberImage(MemberImageDto memberImageDto) {
        this.memberImage = memberImageDto.getMemberImage();
    }
}
