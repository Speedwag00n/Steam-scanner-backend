package ilia.nemankov.steamscan.mapper;

import ilia.nemankov.steamscan.dto.ItemDTO;
import ilia.nemankov.steamscan.dto.ItemStatsDTO;
import ilia.nemankov.steamscan.model.ItemStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemStatsMapper {

    private ItemMapper itemMapper;

    @Autowired
    public ItemStatsMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public ItemStatsDTO entityToDto(ItemStats entity) {
        ItemStatsDTO dto = new ItemStatsDTO();

        ItemDTO itemDTO = itemMapper.entityToDto(entity.getItem());
        dto.setItem(itemDTO);
        dto.setHighestBuyOrder(entity.getHighestBuyOrder());
        dto.setLowestSellOrder(entity.getLowestSellOrder());
        dto.setCurrency(entity.getCurrency());
        dto.setLastUpdate(entity.getLastUpdate());
        dto.setProfitAbsolute(entity.getProfitAbsolute());
        dto.setProfitRelative(entity.getProfitRelative());
        dto.setItemUrl("https://steamcommunity.com/market/listings/" + entity.getGame().getId() + "/" + entity.getItem().getItemName());

        return dto;
    }

}
