package ilia.nemankov.steamscan.mapper;

import ilia.nemankov.steamscan.dto.GameDTO;
import ilia.nemankov.steamscan.dto.ItemDTO;
import ilia.nemankov.steamscan.model.Item;
import ilia.nemankov.steamscan.model.ItemId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    private GameMapper gameMapper;

    @Autowired
    public ItemMapper(GameMapper gameMapper) {
        this.gameMapper = gameMapper;
    }

    public Item dtoToEntity(ItemDTO dto) {
        Item entity = new Item();

        ItemId id = new ItemId();
        id.setItemId(dto.getItemId());
        id.setGameId(dto.getGame().getId());
        entity.setId(id);
        entity.setItemName(dto.getItemName());

        return entity;
    }

    public ItemDTO entityToDto(Item entity) {
        ItemDTO dto = new ItemDTO();

        dto.setItemId(entity.getId().getItemId());
        GameDTO gameDTO = gameMapper.entityToDto(entity.getGame());
        dto.setGame(gameDTO);
        dto.setItemName(entity.getItemName());

        return dto;
    }

}
