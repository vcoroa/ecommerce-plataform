package br.com.vcoroa.ecommerce.platform.presentation.controller;

import br.com.vcoroa.ecommerce.platform.application.dto.request.CreateOrderRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.response.OrderDTO;
import br.com.vcoroa.ecommerce.platform.application.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderDTO order = orderService.createOrder(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderDTO> payOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderDTO order = orderService.payOrder(id, userDetails.getUsername());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderDTO>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<OrderDTO> orders = orderService.getMyOrders(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(orders);
    }
}
