package ilia.nemankov.steamscan.repository;

import ilia.nemankov.steamscan.model.ItemId;
import ilia.nemankov.steamscan.model.ItemStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemStatsRepository extends JpaRepository<ItemStats, ItemId> {

}
