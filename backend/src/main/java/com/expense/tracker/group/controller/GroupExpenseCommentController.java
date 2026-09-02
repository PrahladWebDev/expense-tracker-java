package com.expense.tracker.group.controller;

import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.group.dto.CommentRequest;
import com.expense.tracker.group.dto.CommentResponse;
import com.expense.tracker.group.service.GroupExpenseCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses/{expenseId}/comments")
@RequiredArgsConstructor
public class GroupExpenseCommentController {

    private final GroupExpenseCommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> list(
            Authentication authentication, @PathVariable Long groupId, @PathVariable Long expenseId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.listComments(authentication.getName(), groupId, expenseId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> add(
            Authentication authentication, @PathVariable Long groupId, @PathVariable Long expenseId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.addComment(authentication.getName(), groupId, expenseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Comment added"));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication, @PathVariable Long groupId, @PathVariable Long expenseId, @PathVariable Long commentId) {
        commentService.deleteComment(authentication.getName(), groupId, expenseId, commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted"));
    }
}
