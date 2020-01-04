package ilia.nemankov.steamscan.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GameDTO implements Serializable {

    private static final long serialVersionUID = -4573583376723236182L;

    private long id;

    private String name;

}
