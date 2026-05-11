package uk.co.nationwide.unified.nationwide;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NwCardXrefRepository extends JpaRepository<NwCardXrefEntity, String> {
    List<NwCardXrefEntity> findByXrefCustId(Long custId);
}
