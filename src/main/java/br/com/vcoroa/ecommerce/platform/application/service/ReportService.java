package br.com.vcoroa.ecommerce.platform.application.service;

import br.com.vcoroa.ecommerce.platform.application.dto.response.AvgTicketDTO;
import br.com.vcoroa.ecommerce.platform.application.dto.response.RevenueDTO;
import br.com.vcoroa.ecommerce.platform.application.dto.response.TopUserDTO;
import br.com.vcoroa.ecommerce.platform.application.mapper.ReportMapper;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final ReportMapper reportMapper;

    public List<TopUserDTO> getTopUsers(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Object[]> results = orderRepository.findTopUsersByTotalPurchase(start, end, PageRequest.of(0, 5));

        return results.stream()
            .map(reportMapper::toTopUserDTO)
            .collect(Collectors.toList());
    }

    public List<AvgTicketDTO> getAvgTicketPerUser(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Object[]> results = orderRepository.findAvgTicketPerUser(start, end);

        return results.stream()
            .map(reportMapper::toAvgTicketDTO)
            .collect(Collectors.toList());
    }

    public RevenueDTO getCurrentMonthRevenue() {
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = now.plusMonths(1).withDayOfMonth(1).atStartOfDay().minusSeconds(1);

        BigDecimal total = orderRepository.calculateTotalRevenue(start, end);

        RevenueDTO dto = new RevenueDTO();
        dto.setMonth(now.getMonth().toString());
        dto.setYear(now.getYear());
        dto.setTotalRevenue(total != null ? total : BigDecimal.ZERO);

        return dto;
    }
}
