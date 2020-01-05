package ilia.nemankov.steamscan.repository;

import ilia.nemankov.steamscan.model.ItemId;
import ilia.nemankov.steamscan.model.ItemStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ItemStatsRepository extends JpaRepository<ItemStats, ItemId>, JpaSpecificationExecutor<ItemStats> {

    Page<ItemStats> findAllByGameId(long gameId, Pageable pageable);

    Page<ItemStats> findAllByGameIdIn(Iterable<Long> gameIds, Pageable pageable);

    Page<ItemStats> findAll(Specification<ItemStats> specification, Pageable pageable);

}
