package uk.co.nationwide.unified.nationwide;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NwCustomerRepository extends JpaRepository<NwCustomerEntity, Long> {
}
