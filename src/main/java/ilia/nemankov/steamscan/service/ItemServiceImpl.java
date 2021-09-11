package ilia.nemankov.steamscan.service;

import ilia.nemankov.steamscan.dto.ItemStatsDTO;
import ilia.nemankov.steamscan.dto.ItemStatsSearchDTO;
import ilia.nemankov.steamscan.mapper.ItemStatsMapper;
import ilia.nemankov.steamscan.model.ItemStats;
import ilia.nemankov.steamscan.repository.ItemStatsRepository;
import ilia.nemankov.steamscan.repository.specification.FindItemStatsByGameId;
import ilia.nemankov.steamscan.repository.specification.FindItemStatsByHighestBuyOrder;
import ilia.nemankov.steamscan.repository.specification.Sign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private ItemStatsRepository itemStatsRepository;
    private ItemStatsMapper itemStatsMapper;

    @Autowired
    public ItemServiceImpl(ItemStatsRepository itemStatsRepository, ItemStatsMapper itemStatsMapper) {
        this.itemStatsRepository = itemStatsRepository;
        this.itemStatsMapper = itemStatsMapper;
    }

    @Override
    public List<ItemStatsDTO> getStats(ItemStatsSearchDTO itemStatsSearchDTO) {
        // Set default sort value if it's empty
        if (itemStatsSearchDTO.getDirection() == null) {
            itemStatsSearchDTO.setDirection(Sort.Direction.DESC);
        }

        // Set ID column as default sorting column if it's empty
        if (itemStatsSearchDTO.getColumns() == null || itemStatsSearchDTO.getColumns().length == 0) {
            itemStatsSearchDTO.setColumns(new ItemSortingColumn[]{ ItemSortingColumn.ID });
        }
        String[] columnsString = Arrays.stream(itemStatsSearchDTO.getColumns()).map(item -> item.toString()).toArray(String[]::new);

        Specification<ItemStats> specification = new FindItemStatsByGameId(new Long[]{itemStatsSearchDTO.getGameId()});

        // Specify additional sorting rules
        if (itemStatsSearchDTO.getHighestBuyOrder() != null && itemStatsSearchDTO.getHighestBuyOrderSign() != null) {
            specification = specification.and(new FindItemStatsByHighestBuyOrder(itemStatsSearchDTO.getHighestBuyOrder(), itemStatsSearchDTO.getHighestBuyOrderSign()));
        }
        if (itemStatsSearchDTO.getLowestSellOrder() != null && itemStatsSearchDTO.getLowestSellOrderSign() != null) {
            specification = specification.and(new FindItemStatsByHighestBuyOrder(itemStatsSearchDTO.getLowestSellOrder(), itemStatsSearchDTO.getLowestSellOrderSign()));
        }
        if (itemStatsSearchDTO.getProfitAbsolute() != null && itemStatsSearchDTO.getProfitAbsoluteSign() != null) {
            specification = specification.and(new FindItemStatsByHighestBuyOrder(itemStatsSearchDTO.getProfitAbsolute(), itemStatsSearchDTO.getProfitAbsoluteSign()));
        }
        if (itemStatsSearchDTO.getProfitRelative() != null && itemStatsSearchDTO.getProfitRelativeSign() != null) {
            specification = specification.and(new FindItemStatsByHighestBuyOrder(itemStatsSearchDTO.getProfitRelative(), itemStatsSearchDTO.getProfitRelativeSign()));
        }

        List<ItemStats> itemStats = itemStatsRepository.findAll(
                specification,
                PageRequest.of(
                        itemStatsSearchDTO.getStartPage(),
                        itemStatsSearchDTO.getCount(),
                        Sort.by(itemStatsSearchDTO.getDirection(), columnsString)
                )
        ).getContent();

        return itemStatsMapper.entitiesToDtos(itemStats);
    }

}
