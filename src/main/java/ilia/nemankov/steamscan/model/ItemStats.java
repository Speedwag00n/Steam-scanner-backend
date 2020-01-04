package ilia.nemankov.steamscan.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

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

    @MapsId("gameId")
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="game_id", referencedColumnName="id")
    private Game game;

    private Double highestBuyOrder;

    private Double lowestSellOrder;

    private Integer currency = 1;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    private Double profitAbsolute;

    private Double profitRelative;

}
