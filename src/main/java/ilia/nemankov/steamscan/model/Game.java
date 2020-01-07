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

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    private List<Item> items;

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY)
    private List<ItemStats> itemStats;

}
