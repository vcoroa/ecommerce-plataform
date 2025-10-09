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
import br.com.vcoroa.ecommerce.platform.domain.enums.Role;
import br.com.vcoroa.ecommerce.platform.domain.exception.InvalidOrderStatusException;
import br.com.vcoroa.ecommerce.platform.domain.exception.ResourceNotFoundException;
import br.com.vcoroa.ecommerce.platform.domain.exception.UnauthorizedException;
import br.com.vcoroa.ecommerce.platform.infrastructure.messaging.event.OrderPaidEvent;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.OrderRepository;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductRepository;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, OrderPaidEvent> kafkaTemplate;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        user = User.builder()
                .id(10)
                .uuid(UUID.randomUUID())
                .username("john")
                .email("john@example.com")
                .password("hashed")
                .role(Role.USER)
                .build();

        product = Product.builder()
                .id(99)
                .uuid(productId)
                .name("MacBook")
                .price(BigDecimal.valueOf(1000))
                .stockQuantity(2)
                .category("ELECTRONICS")
                .build();
    }

    @Test
    void createOrder_shouldCancelWhenStockInsufficient() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(productId);
        itemRequest.setQuantity(5);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(Collections.singletonList(itemRequest));

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(productRepository.findByUuid(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO expectedDto = new OrderDTO();
        expectedDto.setStatus(OrderStatus.CANCELLED);
        when(orderMapper.toDTO(any(Order.class))).thenReturn(expectedDto);

        OrderDTO result = orderService.createOrder(request, "john");

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(savedOrder.getItems()).isEmpty();
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void payOrder_shouldMarkAsPaidAndPublishEvent() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setUuid(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(2000));
        ArrayList<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem(order));
        order.setItems(items);

        when(orderRepository.findByUuid(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setPaidAt(LocalDateTime.now());
            return saved;
        });

        OrderDTO expectedDto = new OrderDTO();
        expectedDto.setStatus(OrderStatus.PAID);
        when(orderMapper.toDTO(order)).thenReturn(expectedDto);

        OrderDTO result = orderService.payOrder(orderId, user.getUsername());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();

        ArgumentCaptor<OrderPaidEvent> eventCaptor = ArgumentCaptor.forClass(OrderPaidEvent.class);
        verify(kafkaTemplate).send(eq("order.paid"), eventCaptor.capture());
        OrderPaidEvent event = eventCaptor.getValue();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getItems()).hasSize(1);
        assertThat(event.getItems().get(0).getProductId()).isEqualTo(productId);
        assertThat(event.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    void payOrder_shouldThrowWhenUserDoesNotOwnOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setUuid(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByUuid(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.payOrder(orderId, "other"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("only pay your own orders");

        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void payOrder_shouldThrowWhenOrderNotPending() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setUuid(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findByUuid(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.payOrder(orderId, user.getUsername()))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Order cannot be paid");

        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void payOrder_shouldThrowWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByUuid(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.payOrder(orderId, user.getUsername()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    private OrderItem createOrderItem(Order order) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(1);
        item.setPriceAtPurchase(product.getPrice());
        return item;
    }
}
