package ilia.nemankov.steamscan.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Game {

    @Id
    private long id;

    private String name;

    @OneToOne(mappedBy="game", cascade = CascadeType.ALL)
    private ItemSearchCycle itemSearchCycle;

}
