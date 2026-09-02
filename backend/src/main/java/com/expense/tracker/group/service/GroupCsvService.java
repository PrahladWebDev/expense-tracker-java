package com.expense.tracker.group.service;

import com.expense.tracker.group.dto.GroupExpenseResponse;
import com.expense.tracker.group.dto.MemberBalanceResponse;
import com.expense.tracker.group.dto.SettlementResponse;
import com.expense.tracker.group.entity.ExpenseGroup;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CONCEPT: CSV needs no library
 * Unlike the PDF report (which needs real layout/formatting), a CSV is
 * just comma-separated text - so this writes plain rows with a
 * PrintWriter, no dependency required. Opens directly in Excel, Google
 * Sheets, or Numbers. A leading UTF-8 BOM is written so Excel on Windows
 * renders the ₹ symbol correctly instead of mangling it.
 */
@Service
@RequiredArgsConstructor
public class GroupCsvService {

    private final GroupService groupService;
    private final GroupExpenseService groupExpenseService;
    private final BalanceService balanceService;
    private final SettlementService settlementService;
    private final UserRepository userRepository;

    public byte[] generateGroupCsv(String userEmail, Long groupId) {
        User requester = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ExpenseGroup group = groupService.getGroupEntity(groupId);
        groupService.requireMembership(groupId, requester.getId());

        List<GroupExpenseResponse> expenses = groupExpenseService.listActiveExpenses(userEmail, groupId);
        List<MemberBalanceResponse> balances = balanceService.computeBalances(groupId);
        List<SettlementResponse> settlements = settlementService.listSettlements(userEmail, groupId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xEF); out.write(0xBB); out.write(0xBF); // UTF-8 BOM
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("Group: " + escape(group.getName()));
            writer.println();

            writer.println("EXPENSES");
            writer.println("Date,Description,Paid By,Split Type,Amount,Participant Shares");
            for (GroupExpenseResponse e : expenses) {
                String shares = e.shares().stream()
                        .map(s -> s.fullName() + ": " + s.shareAmount())
                        .reduce((a, b) -> a + " | " + b)
                        .orElse("");
                writer.println(String.join(",",
                        e.expenseDate().toString(),
                        escape(e.description() != null ? e.description() : ""),
                        escape(e.paidByName()),
                        e.splitType(),
                        e.amount().toString(),
                        escape(shares)));
            }
            writer.println();

            writer.println("BALANCES");
            writer.println("Member,Total Paid,Total Share,Net Balance");
            for (MemberBalanceResponse b : balances) {
                writer.println(String.join(",",
                        escape(b.fullName()), b.totalPaid().toString(), b.totalShare().toString(), b.netBalance().toString()));
            }
            writer.println();

            writer.println("SETTLEMENTS");
            writer.println("Date,From,To,Amount,Note");
            for (SettlementResponse s : settlements) {
                writer.println(String.join(",",
                        s.settledAt().toString(), escape(s.fromName()), escape(s.toName()), s.amount().toString(),
                        escape(s.note() != null ? s.note() : "")));
            }
        }
        return out.toByteArray();
    }

    /** Wraps a field in quotes (and escapes inner quotes) only if it contains a comma, quote, or newline. */
    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
