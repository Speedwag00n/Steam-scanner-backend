package ilia.nemankov.steamscan.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ItemDTO implements Serializable {

    private static final long serialVersionUID = -2610181417569281835L;

    private long itemId;

    private GameDTO game;

    private String itemName;

}
