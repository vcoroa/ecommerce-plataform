package br.com.vcoroa.ecommerce.platform.application.service;

import br.com.vcoroa.ecommerce.platform.application.dto.request.CreateOrderRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.request.OrderItemRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.response.OrderDTO;
import br.com.vcoroa.ecommerce.platform.application.mapper.OrderMapper;
import br.com.vcoroa.ecommerce.platform.domain.entity.Order;
import br.com.vcoroa.ecommerce.platform.domain.entity.OrderItem;
import br.com.vcoroa.ecommerce.platform.domain.entity.Product;
import br.com.vcoroa.ecommerce.platform.domain.entity.User;
import br.com.vcoroa.ecommerce.platform.domain.enums.OrderStatus;
import br.com.vcoroa.ecommerce.platform.domain.exception.InvalidOrderStatusException;
import br.com.vcoroa.ecommerce.platform.domain.exception.ResourceNotFoundException;
import br.com.vcoroa.ecommerce.platform.domain.exception.UnauthorizedException;
import br.com.vcoroa.ecommerce.platform.infrastructure.messaging.event.OrderPaidEvent;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.OrderRepository;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductRepository;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, OrderPaidEvent> kafkaTemplate;
    private final OrderMapper orderMapper;

    public OrderDTO createOrder(CreateOrderRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        boolean hasInsufficientStock = false;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByUuid(itemReq.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                hasInsufficientStock = true;
                break;
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPriceAtPurchase(product.getPrice()); // Snapshot do preço

            order.getItems().add(item);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(itemTotal);
        }

        if (hasInsufficientStock) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        return orderMapper.toDTO(saved);
    }

    public OrderDTO payOrder(UUID orderUuid, String username) {
        Order order = orderRepository.findByUuid(orderUuid)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("You can only pay your own orders");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Order cannot be paid. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        Order paid = orderRepository.save(order);

        List<OrderPaidEvent.OrderItemDTO> eventItems = paid.getItems().stream()
            .map(item -> new OrderPaidEvent.OrderItemDTO(
                item.getProduct().getUuid(),
                item.getQuantity()
            ))
            .collect(Collectors.toList());

        OrderPaidEvent event = new OrderPaidEvent(paid.getUuid(), eventItems);
        kafkaTemplate.send("order.paid", event);

        return orderMapper.toDTO(paid);
    }

    public Page<OrderDTO> getMyOrders(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Order> orders = orderRepository.findByUser(user, pageable);
        return orders.map(orderMapper::toDTO);
    }
}
