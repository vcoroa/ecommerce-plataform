package br.com.vcoroa.ecommerce.platform.presentation.controller;

import br.com.vcoroa.ecommerce.platform.application.dto.response.AvgTicketDTO;
import br.com.vcoroa.ecommerce.platform.application.dto.response.RevenueDTO;
import br.com.vcoroa.ecommerce.platform.application.dto.response.TopUserDTO;
import br.com.vcoroa.ecommerce.platform.application.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/top-users")
    public ResponseEntity<List<TopUserDTO>> getTopUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TopUserDTO> result = reportService.getTopUsers(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/avg-ticket")
    public ResponseEntity<List<AvgTicketDTO>> getAvgTicket(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AvgTicketDTO> result = reportService.getAvgTicketPerUser(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/current-month-revenue")
    public ResponseEntity<RevenueDTO> getCurrentMonthRevenue() {
        RevenueDTO result = reportService.getCurrentMonthRevenue();
        return ResponseEntity.ok(result);
    }
}
