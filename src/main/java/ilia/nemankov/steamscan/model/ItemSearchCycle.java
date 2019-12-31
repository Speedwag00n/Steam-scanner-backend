package ilia.nemankov.steamscan.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@Getter
@Setter
public class ItemSearchCycle {

    @Id
    private long gameId;

    @OneToOne
    @PrimaryKeyJoinColumn(name="game_id", referencedColumnName="id")
    private Game game;

    private int nextPage;

    private boolean searchFinished;

}
