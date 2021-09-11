package ilia.nemankov.steamscan.service;

import ilia.nemankov.steamscan.dto.ItemStatsDTO;
import ilia.nemankov.steamscan.dto.ItemStatsSearchDTO;
import ilia.nemankov.steamscan.repository.specification.Sign;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ItemService {

    List<ItemStatsDTO> getStats(ItemStatsSearchDTO itemStatsSearchDTO);

}
