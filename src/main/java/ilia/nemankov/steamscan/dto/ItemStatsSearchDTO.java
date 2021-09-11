package ilia.nemankov.steamscan.dto;

import ilia.nemankov.steamscan.repository.specification.Sign;
import ilia.nemankov.steamscan.service.ItemSortingColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ItemStatsSearchDTO implements Serializable {

    private long gameId;
    private int startPage;
    private int count;
    private Sort.Direction direction;
    private ItemSortingColumn[] columns;

    private Double highestBuyOrder;
    private Sign highestBuyOrderSign;
    private Double lowestSellOrder;
    private Sign lowestSellOrderSign;
    private Double profitAbsolute;
    private Sign profitAbsoluteSign;
    private Double profitRelative;
    private Sign profitRelativeSign;

}
