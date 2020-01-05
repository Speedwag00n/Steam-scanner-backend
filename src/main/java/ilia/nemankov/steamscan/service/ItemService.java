package ilia.nemankov.steamscan.service;

import ilia.nemankov.steamscan.dto.ItemStatsDTO;
import ilia.nemankov.steamscan.repository.specification.Sign;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ItemService {

    List<ItemStatsDTO> getStats(
            long gameId, int startPage, int count, Sort.Direction direction, ItemSortingColumn[] columns,
            Double highestBuyOrder, Sign highestBuyOrderSign,
            Double lowestSellOrder, Sign lowestSellOrderSign,
            Double profitAbsolute, Sign profitAbsoluteSign,
            Double profitRelative, Sign profitRelativeSign
    );

}
