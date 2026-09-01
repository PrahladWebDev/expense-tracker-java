package com.expense.tracker.group.service;

import com.expense.tracker.common.exception.ForbiddenException;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.group.dto.GroupExpenseResponse;
import com.expense.tracker.group.dto.MemberBalanceResponse;
import com.expense.tracker.group.dto.SettlementResponse;
import com.expense.tracker.group.entity.ExpenseGroup;
import com.expense.tracker.group.repository.GroupExpenseShareRepository;
import com.expense.tracker.group.repository.GroupMemberRepository;
import com.expense.tracker.group.repository.SettlementRepository;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CONCEPT: Generating PDFs server-side
 * We use OpenPDF (a maintained fork of the classic iText 2 library) to
 * build documents programmatically: create a Document, open a
 * ByteArrayOutputStream-backed PdfWriter, add Paragraphs/Tables, close it,
 * and return the raw bytes. The controller then streams those bytes back
 * with a `Content-Type: application/pdf` header - no temp files on disk.
 */
@Service
@RequiredArgsConstructor
public class GroupPdfService {

    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(79, 70, 229));
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.DARK_GRAY);
    private static final Font HEADER_CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font MUTED_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
    private static final Font POSITIVE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(21, 128, 61));
    private static final Font NEGATIVE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(185, 28, 28));

    private final GroupService groupService;
    private final GroupExpenseService groupExpenseService;
    private final BalanceService balanceService;
    private final SettlementService settlementService;
    private final GroupMemberRepository memberRepository;
    private final GroupExpenseShareRepository shareRepository;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

    /** Overall group report: every expense, every member's balance, full settlement history. */
    public byte[] generateGroupReport(String userEmail, Long groupId) {
        User requester = requireMembership(userEmail, groupId);
        ExpenseGroup group = groupService.getGroupEntity(groupId);
        groupService.requireMembership(groupId, requester.getId());

        List<GroupExpenseResponse> expenses = groupExpenseService.listExpenses(userEmail, groupId);
        List<MemberBalanceResponse> balances = balanceService.computeBalances(groupId);
        List<SettlementResponse> settlements = settlementService.listSettlements(userEmail, groupId);

        try {
            Document document = new Document(PageSize.A4, 40, 40, 50, 40);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(group.getName(), TITLE_FONT));
            if (group.getDescription() != null && !group.getDescription().isBlank()) {
                document.add(new Paragraph(group.getDescription(), MUTED_FONT));
            }
            document.add(new Paragraph("Group expense report - generated " +
                    TIMESTAMP_FMT.format(java.time.Instant.now().atZone(ZoneId.systemDefault())), MUTED_FONT));
            document.add(Chunk.NEWLINE);

            addSectionHeading(document, "Expenses");
            BigDecimal total = addExpensesTable(document, expenses);
            document.add(new Paragraph("Total group spend: " + formatCurrency(total),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            document.add(Chunk.NEWLINE);

            addSectionHeading(document, "Balances");
            addBalancesTable(document, balances);
            document.add(Chunk.NEWLINE);

            addSectionHeading(document, "Settlement history");
            addSettlementsTable(document, settlements);

            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate PDF report", ex);
        }
    }

    /** Per-member statement: only the expenses/settlements that involve this member, plus their final balance. */
    public byte[] generateMemberStatement(String userEmail, Long groupId, Long memberUserId) {
        User requester = requireMembership(userEmail, groupId);
        ExpenseGroup group = groupService.getGroupEntity(groupId);
        groupService.requireMembership(groupId, requester.getId());

        User member = memberRepository.findByGroupIdAndUserId(groupId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("That user is not a member of this group"))
                .getUser();

        List<GroupExpenseResponse> memberExpenses = groupExpenseService.listExpenses(userEmail, groupId).stream()
                .filter(e -> e.paidByUserId().equals(memberUserId)
                        || e.shares().stream().anyMatch(s -> s.userId().equals(memberUserId)))
                .toList();

        MemberBalanceResponse balance = balanceService.computeBalances(groupId).stream()
                .filter(b -> b.userId().equals(memberUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Balance not found for member"));

        List<SettlementResponse> memberSettlements = settlementService.listSettlements(userEmail, groupId).stream()
                .filter(s -> s.fromUserId().equals(memberUserId) || s.toUserId().equals(memberUserId))
                .toList();

        try {
            Document document = new Document(PageSize.A4, 40, 40, 50, 40);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(group.getName() + " - Statement for " + member.getFullName(), TITLE_FONT));
            document.add(new Paragraph(member.getEmail(), MUTED_FONT));
            document.add(new Paragraph("Generated " +
                    TIMESTAMP_FMT.format(java.time.Instant.now().atZone(ZoneId.systemDefault())), MUTED_FONT));
            document.add(Chunk.NEWLINE);

            Font balanceFont = balance.netBalance().signum() > 0 ? POSITIVE_FONT
                    : balance.netBalance().signum() < 0 ? NEGATIVE_FONT
                    : CELL_FONT;
            String balanceLabel = balance.netBalance().signum() > 0
                    ? "The group owes " + member.getFullName() + " " + formatCurrency(balance.netBalance())
                    : balance.netBalance().signum() < 0
                    ? member.getFullName() + " owes the group " + formatCurrency(balance.netBalance().abs())
                    : member.getFullName() + " is settled up";
            document.add(new Paragraph(balanceLabel, balanceFont));
            document.add(new Paragraph("Total paid: " + formatCurrency(balance.totalPaid())
                    + "    |    Total share of expenses: " + formatCurrency(balance.totalShare()), MUTED_FONT));
            document.add(Chunk.NEWLINE);

            addSectionHeading(document, "Expenses involving " + member.getFullName());
            addExpensesTable(document, memberExpenses);
            document.add(Chunk.NEWLINE);

            addSectionHeading(document, "Settlements involving " + member.getFullName());
            addSettlementsTable(document, memberSettlements);

            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate PDF statement", ex);
        }
    }

    private void addSectionHeading(Document document, String text) throws DocumentException {
        document.add(new Paragraph(text, SECTION_FONT));
        document.add(Chunk.NEWLINE);
    }

    private BigDecimal addExpensesTable(Document document, List<GroupExpenseResponse> expenses) throws DocumentException {
        BigDecimal total = BigDecimal.ZERO;
        PdfPTable table = new PdfPTable(new float[]{2f, 3f, 2.5f, 1.5f, 3f});
        table.setWidthPercentage(100);
        for (String h : List.of("Date", "Description", "Paid by", "Amount", "Split")) {
            addHeaderCell(table, h);
        }
        if (expenses.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No expenses recorded", MUTED_FONT));
            empty.setColspan(5);
            empty.setPadding(6);
            table.addCell(empty);
        }
        for (GroupExpenseResponse e : expenses) {
            addCell(table, e.expenseDate().toString());
            addCell(table, e.description() == null || e.description().isBlank() ? "-" : e.description());
            addCell(table, e.paidByName());
            addCell(table, formatCurrency(e.amount()));
            String splitSummary = e.shares().stream()
                    .map(s -> s.fullName() + ": " + formatCurrency(s.shareAmount()))
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("-");
            addCell(table, splitSummary);
            total = total.add(e.amount());
        }
        document.add(table);
        return total;
    }

    private void addBalancesTable(Document document, List<MemberBalanceResponse> balances) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{3f, 2f, 2f, 2f});
        table.setWidthPercentage(100);
        for (String h : List.of("Member", "Total paid", "Total share", "Net balance")) {
            addHeaderCell(table, h);
        }
        for (MemberBalanceResponse b : balances) {
            addCell(table, b.fullName());
            addCell(table, formatCurrency(b.totalPaid()));
            addCell(table, formatCurrency(b.totalShare()));
            Font f = b.netBalance().signum() > 0 ? POSITIVE_FONT : b.netBalance().signum() < 0 ? NEGATIVE_FONT : CELL_FONT;
            String label = b.netBalance().signum() > 0 ? "+" + formatCurrency(b.netBalance())
                    : b.netBalance().signum() < 0 ? "-" + formatCurrency(b.netBalance().abs())
                    : formatCurrency(BigDecimal.ZERO);
            PdfPCell cell = new PdfPCell(new Phrase(label, f));
            cell.setPadding(6);
            table.addCell(cell);
        }
        document.add(table);
    }

    private void addSettlementsTable(Document document, List<SettlementResponse> settlements) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{2f, 3f, 3f, 2f, 3f});
        table.setWidthPercentage(100);
        for (String h : List.of("Date", "From", "To", "Amount", "Note")) {
            addHeaderCell(table, h);
        }
        if (settlements.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No settlements recorded yet", MUTED_FONT));
            empty.setColspan(5);
            empty.setPadding(6);
            table.addCell(empty);
        }
        for (SettlementResponse s : settlements) {
            addCell(table, TIMESTAMP_FMT.format(s.settledAt().atZone(ZoneId.systemDefault())));
            addCell(table, s.fromName());
            addCell(table, s.toName());
            addCell(table, formatCurrency(s.amount()));
            addCell(table, s.note() == null || s.note().isBlank() ? "-" : s.note());
        }
        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_CELL_FONT));
        cell.setBackgroundColor(new Color(79, 70, 229));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private String formatCurrency(BigDecimal amount) {
        return "Rs. " + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private User requireMembership(String userEmail, Long groupId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!memberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new ForbiddenException("You are not a member of this group");
        }
        return user;
    }
}
