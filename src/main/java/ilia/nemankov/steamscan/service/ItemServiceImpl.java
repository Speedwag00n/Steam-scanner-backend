package ilia.nemankov.steamscan.service;

import ilia.nemankov.steamscan.dto.ItemStatsDTO;
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
    public List<ItemStatsDTO> getStats(
            long gameId, int startPage, int count, Sort.Direction direction, ItemSortingColumn[] columns,
            Double highestBuyOrder, Sign highestBuyOrderSign,
            Double lowestSellOrder, Sign lowestSellOrderSign,
            Double profitAbsolute, Sign profitAbsoluteSign,
            Double profitRelative, Sign profitRelativeSign
    ) {
        if (direction == null) {
            direction = Sort.Direction.DESC;
        }
        if (columns == null || columns.length == 0) {
            columns = new ItemSortingColumn[]{ ItemSortingColumn.ID };
        }
        String[] columnsString = Arrays.stream(columns).map(item -> item.toString()).toArray(String[]::new);

        Specification<ItemStats> specification = new FindItemStatsByGameId(new Long[]{gameId});
        if (highestBuyOrder != null && highestBuyOrderSign != null) {
            specification = specification.and(new FindItemStatsByHighestBuyOrder(highestBuyOrder, highestBuyOrderSign));
        }
        if (lowestSellOrder != null && lowestSellOrderSign != null) {
            specification = specification.and(new FindItemStatsByHighestBuyOrder(lowestSellOrder, lowestSellOrderSign));
        }
        if (profitAbsolute != null && profitAbsoluteSign != null) {
            specification = specification.and(new FindItemStatsByHighestBuyOrder(profitAbsolute, profitAbsoluteSign));
        }
        if (profitRelative != null && profitRelativeSign != null) {
            specification = specification.and(new FindItemStatsByHighestBuyOrder(profitRelative, profitRelativeSign));
        }

        List<ItemStats> itemStats = itemStatsRepository.findAll(specification, PageRequest.of(startPage, count, Sort.by(direction, columnsString))).getContent();
        return itemStatsMapper.entitiesToDtos(itemStats);
    }

}
