package br.com.vcoroa.ecommerce.platform.infrastructure.messaging.consumer;

import br.com.vcoroa.ecommerce.platform.domain.entity.Product;
import br.com.vcoroa.ecommerce.platform.domain.exception.ResourceNotFoundException;
import br.com.vcoroa.ecommerce.platform.infrastructure.messaging.event.OrderPaidEvent;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductElasticsearchRepository;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderPaidConsumer {

    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository elasticsearchRepository;

    @KafkaListener(topics = "order.paid", groupId = "stock-update-group")
    @Transactional
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Processing order paid event for order: {}", event.getOrderId());

        for (OrderPaidEvent.OrderItemDTO item : event.getItems()) {
            Product product = productRepository.findByUuid(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));

            int calculatedStock = product.getStockQuantity() - item.getQuantity();
            final int newStock = Math.max(calculatedStock, 0);

            if (calculatedStock < 0) {
                log.error("Negative stock for product {}: {}", product.getUuid(), calculatedStock);
            }

            product.setStockQuantity(newStock);
            productRepository.save(product);

            elasticsearchRepository.findById(product.getUuid().toString())
                .ifPresent(doc -> {
                    doc.setStockQuantity(newStock);
                    elasticsearchRepository.save(doc);
                });

            log.info("Updated stock for product {}: {}", product.getUuid(), newStock);
        }
    }
}
