package ilia.nemankov.steamscan.controller;

import ilia.nemankov.steamscan.dto.ItemStatsDTO;
import ilia.nemankov.steamscan.repository.specification.Sign;
import ilia.nemankov.steamscan.service.ItemService;
import ilia.nemankov.steamscan.service.ItemSortingColumn;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/item")
public class ItemController {

    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("")
    public List<ItemStatsDTO> getStats(
            @RequestParam(value = "game_id") long gameId,
            @RequestParam(value = "start") int startPage,
            @RequestParam(value = "count") int count,
            @RequestParam(value = "direction", required = false) Sort.Direction direction,
            @RequestParam(value = "columns[]", required = false) ItemSortingColumn[] columns,

            @RequestParam(value = "highestBuyOrder", required = false) Double highestBuyOrder,
            @RequestParam(value = "highestBuyOrderSign", required = false) Sign highestBuyOrderSign,
            @RequestParam(value = "lowestSellOrder", required = false) Double lowestSellOrder,
            @RequestParam(value = "lowestSellOrderSign", required = false) Sign lowestSellOrderSign,
            @RequestParam(value = "profitAbsolute", required = false) Double profitAbsolute,
            @RequestParam(value = "profitAbsoluteSign", required = false) Sign profitAbsoluteSign,
            @RequestParam(value = "profitRelative", required = false) Double profitRelative,
            @RequestParam(value = "profitRelativeSign", required = false) Sign profitRelativeSign
            ) {
        return itemService.getStats(
                gameId, startPage, count, direction, columns,
                highestBuyOrder, highestBuyOrderSign,
                lowestSellOrder, lowestSellOrderSign,
                profitAbsolute, profitAbsoluteSign,
                profitRelative, profitRelativeSign
        );
    }

}
