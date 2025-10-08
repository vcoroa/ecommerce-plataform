package br.com.vcoroa.ecommerce.platform.infrastructure.repository;

import br.com.vcoroa.ecommerce.platform.domain.entity.Order;
import br.com.vcoroa.ecommerce.platform.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByUuid(UUID uuid);

    Page<Order> findByUser(User user, Pageable pageable);
}
