package ilia.nemankov.steamscan.repository;

import ilia.nemankov.steamscan.model.ItemSearchCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemSearchCycleRepository extends JpaRepository<ItemSearchCycle, Long> {

    @Modifying
    @Query("update ItemSearchCycle c set c.nextItem = 0, c.searchFinished = false")
    void initNewCycle();

}
