package ilia.nemankov.steamscan.service;

import ilia.nemankov.steamscan.dto.ItemStatsDTO;
import ilia.nemankov.steamscan.mapper.ItemStatsMapper;
import ilia.nemankov.steamscan.model.ItemStats;
import ilia.nemankov.steamscan.repository.ItemStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    public List<ItemStatsDTO> getStats(long gameId, int startPage, int count) {
        List<ItemStats> itemStats = itemStatsRepository.findAllByGameId(gameId, PageRequest.of(startPage, count, Sort.by(Sort.Direction.DESC, "profitAbsolute"))).getContent();
        return itemStatsMapper.entitiesToDtos(itemStats);
    }

}
