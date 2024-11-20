package com.newbie.board.usedBoard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Table(name = "used_board_comment")
@Builder(toBuilder = true)
public class UsedBoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "used_board_id", nullable = false)
    private UsedBoard usedBoard;

    private Long userId;

    private String userName;

    private String profile;

    @NotNull
    private String content;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private UsedBoardComment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UsedBoardComment> replies = new ArrayList<>();

    @Builder.Default
    private String isDeleted = "N";
}