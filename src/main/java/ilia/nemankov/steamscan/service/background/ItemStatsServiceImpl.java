package ilia.nemankov.steamscan.service.background;

import ilia.nemankov.steamscan.model.ItemStats;
import ilia.nemankov.steamscan.repository.ItemRepository;
import ilia.nemankov.steamscan.repository.ItemStatsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class ItemStatsServiceImpl implements ItemStatsService {

    private static final int PAGE_SIZE = 100;

    private ItemRepository itemRepository;
    private ThreadPoolTaskExecutor itemStatsExecutor;
    private ItemStatsRepository itemStatsRepository;

    private int requestsCount;
    private int pageNumber;
    private List<ItemStats> itemStats;
    private Iterator<ItemStats> iterator;

    @Autowired
    public ItemStatsServiceImpl(ItemRepository itemRepository, ThreadPoolTaskExecutor itemStatsExecutor, ItemStatsRepository itemStatsRepository) {
        this.itemRepository = itemRepository;
        this.itemStatsExecutor = itemStatsExecutor;
        this.itemStatsRepository = itemStatsRepository;

        this.requestsCount = itemStatsExecutor.getMaxPoolSize();
        updateCachedStats();
    }

    @Scheduled(fixedDelay = 5 * 1000)
    private void updateItemStats() {
        for (int i = 0; i < requestsCount; i++) {
            if (iterator.hasNext()) {
                try {
                    itemStatsExecutor.execute(new UpdateItemStats(iterator.next()));
                } catch (TaskRejectedException e) {
                    log.warn("Queue is full", e);
                    return;
                }
            } else {
                updateCachedStats();
                if (itemStats.isEmpty()) {
                    pageNumber = 0;
                    return;
                }
            }
        }
    }

    private void updateCachedStats() {
        itemStats = itemStatsRepository.findAll(PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("itemId"))).getContent();
        iterator = itemStats.iterator();
        pageNumber++;
    }

    @AllArgsConstructor
    class UpdateItemStats implements Runnable {

        private ItemStats stats;

        @Override
        public void run() {
            JSONObject jsonObject;
            try {
                jsonObject = getStats();
            } catch (IOException e) {
                log.warn("Could not get item stats with id {}", stats.getId().getItemId(), e);
                return;
            }

            try {
                updateHighestBuyOrder(jsonObject, stats);
                updateLowestSellOrder(jsonObject, stats);
                stats.setLastUpdate(new Date());
            } catch (Exception e) {
                log.warn("Unexpected error during stats updating happened", e);
                return;
            }

            itemStatsRepository.save(stats);
        }

        private JSONObject getStats() throws IOException {
            String url = buildStatsUrl();
            String data = Jsoup.connect(url).ignoreContentType(true).execute().body();

            return new JSONObject(data);
        }

        private String buildStatsUrl() {
            return "https://steamcommunity.com/market/itemordershistogram?language=english&currency=1&item_nameid=" + stats.getId().getItemId();
        }

        private ItemStats updateHighestBuyOrder(JSONObject jsonObject, ItemStats itemStats) {
            Object data = null;
            double highestBuyOrder;
            try {
                data = jsonObject.getJSONArray("buy_order_graph").getJSONArray(0).get(0);
                highestBuyOrder = (Double) data;
            } catch (ClassCastException e) {
                highestBuyOrder = (Integer) data;
            } catch (JSONException e) {
                highestBuyOrder = 0;
            }

            itemStats.setHighestBuyOrder(highestBuyOrder);
            return itemStats;
        }

        private ItemStats updateLowestSellOrder(JSONObject jsonObject, ItemStats itemStats) {
            Object data = null;
            double lowestSellOrder;
            try {
                data = jsonObject.getJSONArray("sell_order_graph").getJSONArray(0).get(0);
                lowestSellOrder = (Double) data;
            } catch (ClassCastException e) {
                lowestSellOrder = (Integer) data;
            } catch (JSONException e) {
                lowestSellOrder = 0;
            }

            itemStats.setLowestSellOrder(lowestSellOrder);
            return itemStats;
        }

    }

}