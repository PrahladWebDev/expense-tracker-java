package com.expense.tracker.group.service;

import com.expense.tracker.common.exception.ForbiddenException;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.group.dto.CommentRequest;
import com.expense.tracker.group.dto.CommentResponse;
import com.expense.tracker.group.entity.GroupActivityType;
import com.expense.tracker.group.entity.GroupExpense;
import com.expense.tracker.group.entity.GroupExpenseComment;
import com.expense.tracker.group.repository.GroupExpenseCommentRepository;
import com.expense.tracker.group.repository.GroupExpenseRepository;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupExpenseCommentService {

    private static final int NOTE_PREVIEW_LENGTH = 40;

    private final GroupExpenseCommentRepository commentRepository;
    private final GroupExpenseRepository groupExpenseRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final GroupActivityService activityService;

    public List<CommentResponse> listComments(String userEmail, Long groupId, Long expenseId) {
        User user = requireMembership(userEmail, groupId);
        GroupExpense expense = getExpenseEntity(groupId, expenseId);
        return commentRepository.findByGroupExpenseIdOrderByCreatedAtAsc(expense.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(String userEmail, Long groupId, Long expenseId, CommentRequest request) {
        User user = requireMembership(userEmail, groupId);
        GroupExpense expense = getExpenseEntity(groupId, expenseId);
        groupService.requireOpen(expense.getGroup());

        GroupExpenseComment comment = GroupExpenseComment.builder()
                .groupExpense(expense)
                .user(user)
                .text(request.text())
                .build();
        comment = commentRepository.save(comment);

        String preview = request.text().length() > NOTE_PREVIEW_LENGTH
                ? request.text().substring(0, NOTE_PREVIEW_LENGTH) + "…"
                : request.text();
        activityService.log(expense.getGroup(), user, GroupActivityType.COMMENT_ADDED,
                user.getFullName() + " commented on \"" + expense.getDescription() + "\": " + preview);

        return toResponse(comment);
    }

    @Transactional
    public void deleteComment(String userEmail, Long groupId, Long expenseId, Long commentId) {
        User user = requireMembership(userEmail, groupId);
        GroupExpense expense = getExpenseEntity(groupId, expenseId);
        groupService.requireOpen(expense.getGroup());

        GroupExpenseComment comment = commentRepository.findByIdAndGroupExpenseId(commentId, expense.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(GroupExpenseComment c) {
        return new CommentResponse(c.getId(), c.getUser().getId(), c.getUser().getFullName(), c.getText(), c.getCreatedAt());
    }

    private GroupExpense getExpenseEntity(Long groupId, Long expenseId) {
        return groupExpenseRepository.findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group expense not found"));
    }

    private User requireMembership(String userEmail, Long groupId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        groupService.requireMembership(groupId, user.getId());
        return user;
    }
}
