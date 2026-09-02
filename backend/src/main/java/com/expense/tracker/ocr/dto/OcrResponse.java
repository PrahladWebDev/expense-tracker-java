package com.expense.tracker.ocr.dto;

import java.math.BigDecimal;

/**
 * Best-effort extraction from a receipt photo. `suggestedAmount` and
 * `suggestedCategory` are guesses the frontend pre-fills into the expense
 * form - the user always reviews and can overwrite them before saving,
 * nothing is auto-submitted.
 */
public record OcrResponse(
        String rawText,
        BigDecimal suggestedAmount,
        String suggestedCategory,
        String suggestedDescription
) {}
