package ilia.nemankov.steamscan.repository;

import ilia.nemankov.steamscan.model.Item;
import ilia.nemankov.steamscan.model.ItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, ItemId> {

}
