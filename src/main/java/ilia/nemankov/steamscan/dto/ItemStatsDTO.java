package ilia.nemankov.steamscan.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ItemStatsDTO implements Serializable {

    private static final long serialVersionUID = 7648309607188116118L;

    private ItemDTO item;

    private Double highestBuyOrder;

    private Double lowestSellOrder;

    private Integer currency;

    private Date lastUpdate;

    private Double profitAbsolute;

    private Double profitRelative;

    private String itemUrl;

}
