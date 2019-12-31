package ilia.nemankov.steamscan.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Game {

    @Id
    private long id;

    private String name;

    @OneToOne(mappedBy="game")
    private ItemSearchCycle itemSearchCycle;

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    private List<Item> items;

}
