package ilia.nemankov.steamscan.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
public class Item implements Serializable {

    private static final long serialVersionUID = -7316446521334299209L;

    @EmbeddedId
    private ItemId id;

    @MapsId("gameId")
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="game_id", referencedColumnName="id")
    private Game game;

    private String itemName;

    @OneToOne(mappedBy="item")
    private ItemStats itemStats;

}
