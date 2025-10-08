package br.com.vcoroa.ecommerce.platform.infrastructure.repository;

import br.com.vcoroa.ecommerce.platform.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUuid(UUID uuid);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
