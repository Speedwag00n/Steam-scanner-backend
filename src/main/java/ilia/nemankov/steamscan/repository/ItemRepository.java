package ilia.nemankov.steamscan.repository;

import ilia.nemankov.steamscan.model.Game;
import ilia.nemankov.steamscan.model.Item;
import ilia.nemankov.steamscan.model.ItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<Item, ItemId> {

    Set<Item> findByGameAndItemNameIn(Game game, Iterable<String> names);

}
