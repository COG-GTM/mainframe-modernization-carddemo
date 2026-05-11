package uk.co.nationwide.unified.virginmoney;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VmSavingsAccountRepository extends JpaRepository<VmSavingsAccountEntity, Long> {
}
