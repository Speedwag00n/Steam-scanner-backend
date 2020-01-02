package ilia.nemankov.steamscan.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ItemStats {

    @EmbeddedId
    private ItemId id;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumns({
            @PrimaryKeyJoinColumn(name="game_id", referencedColumnName="game_id"),
            @PrimaryKeyJoinColumn(name="item_id", referencedColumnName="item_id")
    })
    private Item item;

    private double highestBuyOrder;

    private double lowestSellOrder;

}
