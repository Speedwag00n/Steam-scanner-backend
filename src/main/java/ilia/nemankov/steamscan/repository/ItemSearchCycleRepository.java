package ilia.nemankov.steamscan.repository;

import ilia.nemankov.steamscan.model.ItemSearchCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemSearchCycleRepository extends JpaRepository<ItemSearchCycle, Long> {

}
