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

    @Id
    private long itemId;

    @Id
    private long gameId;

    private String itemName;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "id")
    private Game game;

}
