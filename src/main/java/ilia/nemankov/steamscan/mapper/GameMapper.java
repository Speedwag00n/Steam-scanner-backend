package ilia.nemankov.steamscan.mapper;

import ilia.nemankov.steamscan.dto.GameDTO;
import ilia.nemankov.steamscan.model.Game;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    public Game dtoToEntity(GameDTO dto) {
        Game entity = new Game();

        entity.setId(dto.getId());
        entity.setName(dto.getName());

        return entity;
    }

    public GameDTO entityToDto(Game entity) {
        GameDTO dto = new GameDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());

        return dto;
    }

}
