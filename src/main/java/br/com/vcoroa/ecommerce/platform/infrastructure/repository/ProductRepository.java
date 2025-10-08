package br.com.vcoroa.ecommerce.platform.infrastructure.repository;

import br.com.vcoroa.ecommerce.platform.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findByUuid(UUID uuid);
}
