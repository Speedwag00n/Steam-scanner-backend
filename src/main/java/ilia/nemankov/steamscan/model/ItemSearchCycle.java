package ilia.nemankov.steamscan.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ItemSearchCycle {

    public ItemSearchCycle(long gameId) {
        this.gameId = gameId;
    }

    @Id
    private long gameId;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name="game_id", referencedColumnName="id")
    private Game game;

    private int nextItem;

    private boolean searchFinished = false;

}
