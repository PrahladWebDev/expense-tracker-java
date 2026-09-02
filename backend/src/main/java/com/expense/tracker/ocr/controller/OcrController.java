package com.expense.tracker.ocr.controller;

import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.ocr.dto.OcrResponse;
import com.expense.tracker.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Any authenticated user can scan a receipt - it's a stateless read
 * (nothing is saved here), used to pre-fill the personal or group expense
 * form. Saving the actual receipt photo is a separate step, done via
 * GroupExpenseController's /receipt endpoint once the expense exists.
 */
@RestController
@RequestMapping("/api/v1/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/receipt")
    public ResponseEntity<ApiResponse<OcrResponse>> scanReceipt(@RequestParam("file") MultipartFile file) {
        OcrResponse response = ocrService.extract(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Receipt scanned"));
    }
}
