package ilia.nemankov.steamscan.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ItemSearchCycle {

    @Id
    private long gameId;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name="game_id", referencedColumnName="id")
    private Game game;

    private int nextPage;

    private boolean searchFinished;

}
