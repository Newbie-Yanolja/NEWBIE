package com.newbie.board.usedBoard.controller;

import com.newbie.board.usedBoard.dto.UsedBoardCommentRequestDto;
import com.newbie.board.usedBoard.dto.UsedBoardCommentResponseDto;
import com.newbie.board.usedBoard.service.UsedBoardCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/used-comment")
@RequiredArgsConstructor
public class UsedBoardCommentController {

    private final UsedBoardCommentService commentService;

    @Operation(summary = "게시글 댓글 조회", description = "게시글에 대한 댓글을 조회합니다.")
    @GetMapping("/{boardId}")
    public ResponseEntity<List<UsedBoardCommentResponseDto>> getComments(@PathVariable @Parameter(description = "boardId") Long boardId) {
        return ResponseEntity.ok(commentService.getCommentList(boardId));
    }

    @Operation(summary = "유저 댓글 생성", description = "유저가 댓글을 작성합니다.")
    @PostMapping("/{boardId}")
    public ResponseEntity<UsedBoardCommentResponseDto> createComment(
            @RequestBody @Parameter(description = "boardId, content") UsedBoardCommentRequestDto requestDto,
            @RequestHeader("X-Member-ID") String userId) {
        UsedBoardCommentResponseDto createdComment = commentService.createComment(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @Operation(summary = "유저 댓글 삭제", description = "유저가 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable @Parameter(description = "commentId") Long commentId,
                                              @RequestHeader("X-Member-ID") String userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}