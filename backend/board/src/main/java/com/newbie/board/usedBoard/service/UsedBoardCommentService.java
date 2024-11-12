package com.newbie.board.usedBoard.service;

import com.newbie.board.usedBoard.dto.UsedBoardCommentRequestDto;
import com.newbie.board.usedBoard.dto.UsedBoardCommentResponseDto;
import com.newbie.board.usedBoard.entity.UsedBoardComment;
import com.newbie.board.usedBoard.entity.UsedBoard;
import com.newbie.board.usedBoard.repository.UsedBoardCommentRepository;
import com.newbie.board.usedBoard.repository.UsedBoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsedBoardCommentService {

    private final UsedBoardCommentRepository commentRepository;
    private final UsedBoardRepository usedBoardRepository;

    /**
     * 댓글 목록을 계층적으로 가져옵니다.
     * @param boardId
     * @return
     */
    public List<UsedBoardCommentResponseDto> getCommentList(Long boardId) {
        List<UsedBoardComment> usedBoardComments = commentRepository.findAllActiveCommentsByBoardId(boardId);
        return usedBoardComments.stream()
                .filter(usedBoardComment -> usedBoardComment.getParent() == null)
                .map(UsedBoardCommentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 댓글을 생성합니다.
     * @param requestDto
     * @param userId
     * @return
     */
    @Transactional
    public UsedBoardCommentResponseDto createComment(UsedBoardCommentRequestDto requestDto, Long userId) {
        UsedBoard usedBoard = usedBoardRepository.findById(requestDto.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found"));

        UsedBoardComment comment = UsedBoardComment.builder()
                .content(requestDto.getContent())
                .createdAt(LocalDateTime.now())
                .userId(userId)
                .usedBoard(usedBoard)
                .isDeleted("N")
                .build();

        if (requestDto.getParentId() != null) {
            UsedBoardComment parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parentComment);
        }

        UsedBoardComment savedComment = commentRepository.save(comment);
        return UsedBoardCommentResponseDto.fromEntity(savedComment);
    }

    /**
     * 댓글을 삭제합니다.
     * @param commentId
     * @param userId
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        UsedBoardComment usedBoardComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!usedBoardComment.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have permission to delete this comment.");
        }

        UsedBoardComment updatedUsedBoardComment = usedBoardComment.toBuilder()
                .isDeleted("Y")
                .build();

        commentRepository.save(updatedUsedBoardComment);
    }
}
