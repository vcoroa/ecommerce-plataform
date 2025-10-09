package br.com.vcoroa.ecommerce.platform.infrastructure.repository;

import br.com.vcoroa.ecommerce.platform.domain.entity.Order;
import br.com.vcoroa.ecommerce.platform.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByUuid(UUID uuid);

    Page<Order> findByUser(User user, Pageable pageable);

    @Query("SELECT o.user.uuid, o.user.username, SUM(o.totalAmount), COUNT(o) " +
           "FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND o.paidAt BETWEEN :startDate AND :endDate " +
           "GROUP BY o.user.uuid, o.user.username " +
           "ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findTopUsersByTotalPurchase(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT o.user.uuid, o.user.username, AVG(o.totalAmount), COUNT(o) " +
           "FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND o.paidAt BETWEEN :startDate AND :endDate " +
           "GROUP BY o.user.uuid, o.user.username " +
           "ORDER BY o.user.username")
    List<Object[]> findAvgTicketPerUser(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
           "WHERE o.status = 'PAID' " +
           "AND o.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenue(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
