package com.expense.tracker.group.controller;

import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.group.dto.*;
import com.expense.tracker.group.service.BalanceService;
import com.expense.tracker.group.service.GroupActivityService;
import com.expense.tracker.group.service.GroupCsvService;
import com.expense.tracker.group.service.GroupPdfService;
import com.expense.tracker.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final BalanceService balanceService;
    private final GroupPdfService groupPdfService;
    private final GroupCsvService groupCsvService;
    private final GroupActivityService groupActivityService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> create(
            Authentication authentication, @Valid @RequestBody GroupRequest request) {
        GroupResponse response = groupService.createGroup(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Group created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> listMine(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(groupService.listMyGroups(authentication.getName())));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> getOne(Authentication authentication, @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroup(authentication.getName(), groupId)));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication authentication, @PathVariable Long groupId) {
        groupService.deleteGroup(authentication.getName(), groupId);
        return ResponseEntity.ok(ApiResponse.success(null, "Group deleted"));
    }

    /** Closes the group: read-only from here on (no new expenses/members/settlements) until reopened. */
    @PostMapping("/{groupId}/close")
    public ResponseEntity<ApiResponse<GroupResponse>> close(Authentication authentication, @PathVariable Long groupId) {
        GroupResponse response = groupService.closeGroup(authentication.getName(), groupId);
        return ResponseEntity.ok(ApiResponse.success(response, "Group closed"));
    }

    /** Reopens a closed group, restoring normal read/write access. */
    @PostMapping("/{groupId}/reopen")
    public ResponseEntity<ApiResponse<GroupResponse>> reopen(Authentication authentication, @PathVariable Long groupId) {
        GroupResponse response = groupService.reopenGroup(authentication.getName(), groupId);
        return ResponseEntity.ok(ApiResponse.success(response, "Group reopened"));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupResponse>> addMember(
            Authentication authentication, @PathVariable Long groupId, @Valid @RequestBody AddMemberRequest request) {
        GroupResponse response = groupService.addMember(authentication.getName(), groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Member added"));
    }

    @DeleteMapping("/{groupId}/members/{memberUserId}")
    public ResponseEntity<ApiResponse<GroupResponse>> removeMember(
            Authentication authentication, @PathVariable Long groupId, @PathVariable Long memberUserId) {
        GroupResponse response = groupService.removeMember(authentication.getName(), groupId, memberUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "Member removed"));
    }

    @GetMapping("/{groupId}/balances")
    public ResponseEntity<ApiResponse<List<MemberBalanceResponse>>> balances(
            Authentication authentication, @PathVariable Long groupId) {
        groupService.getGroup(authentication.getName(), groupId); // enforces membership
        return ResponseEntity.ok(ApiResponse.success(balanceService.computeBalances(groupId)));
    }

    /** Recent "what happened" feed for the group - expenses, members, settlements, comments. */
    @GetMapping("/{groupId}/activity")
    public ResponseEntity<ApiResponse<List<GroupActivityResponse>>> activity(
            Authentication authentication, @PathVariable Long groupId) {
        groupService.getGroup(authentication.getName(), groupId); // enforces membership
        return ResponseEntity.ok(ApiResponse.success(groupActivityService.listActivity(groupId)));
    }

    /** Owner-only: invalidates the current invite link and issues a fresh code. */
    @PostMapping("/{groupId}/invite-code/regenerate")
    public ResponseEntity<ApiResponse<GroupResponse>> regenerateInviteCode(
            Authentication authentication, @PathVariable Long groupId) {
        GroupResponse response = groupService.regenerateInviteCode(authentication.getName(), groupId);
        return ResponseEntity.ok(ApiResponse.success(response, "Invite link regenerated"));
    }

    /**
     * Joins a group via its invite link/code. Deliberately does NOT require
     * the caller to already be a member - that's the whole point, it lets
     * a friend who isn't in anyone's contacts join directly.
     */
    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<ApiResponse<GroupResponse>> joinByInviteCode(
            Authentication authentication, @PathVariable String inviteCode) {
        GroupResponse response = groupService.joinByInviteCode(authentication.getName(), inviteCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Joined group"));
    }

    /** Full group report: every expense, every member's balance, full settlement history. */
    @GetMapping("/{groupId}/report/pdf")
    public ResponseEntity<byte[]> groupReportPdf(Authentication authentication, @PathVariable Long groupId) {
        byte[] pdf = groupPdfService.generateGroupReport(authentication.getName(), groupId);
        return pdfResponse(pdf, "group-" + groupId + "-report.pdf");
    }

    /** Same report as the PDF, in CSV form - opens directly in Excel/Sheets for further crunching. */
    @GetMapping("/{groupId}/report/csv")
    public ResponseEntity<byte[]> groupReportCsv(Authentication authentication, @PathVariable Long groupId) {
        byte[] csv = groupCsvService.generateGroupCsv(authentication.getName(), groupId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"group-" + groupId + "-report.csv\"")
                .body(csv);
    }

    /** Per-member statement: just that member's expenses, settlements, and final balance. */
    @GetMapping("/{groupId}/members/{memberUserId}/statement/pdf")
    public ResponseEntity<byte[]> memberStatementPdf(
            Authentication authentication, @PathVariable Long groupId, @PathVariable Long memberUserId) {
        byte[] pdf = groupPdfService.generateMemberStatement(authentication.getName(), groupId, memberUserId);
        return pdfResponse(pdf, "group-" + groupId + "-member-" + memberUserId + "-statement.pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
