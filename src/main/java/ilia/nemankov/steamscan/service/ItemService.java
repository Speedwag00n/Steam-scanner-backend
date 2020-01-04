package ilia.nemankov.steamscan.service;

import ilia.nemankov.steamscan.dto.ItemStatsDTO;

import java.util.List;

public interface ItemService {

    List<ItemStatsDTO> getStats(long gameId, int startPage, int count);

}
