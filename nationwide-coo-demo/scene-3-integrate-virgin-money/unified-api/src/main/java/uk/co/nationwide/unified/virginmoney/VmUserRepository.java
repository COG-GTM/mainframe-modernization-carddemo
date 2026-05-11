package uk.co.nationwide.unified.virginmoney;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VmUserRepository extends JpaRepository<VmUserEntity, Long> {
    Optional<VmUserEntity> findByEmail(String email);
    Optional<VmUserEntity> findByUsername(String username);
}
