package ilia.nemankov.steamscan.controller;

import ilia.nemankov.steamscan.dto.ItemStatsDTO;
import ilia.nemankov.steamscan.service.ItemService;
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
            @RequestParam(value = "count") int count
    ) {
        return itemService.getStats(gameId, startPage, count);
    }

}
