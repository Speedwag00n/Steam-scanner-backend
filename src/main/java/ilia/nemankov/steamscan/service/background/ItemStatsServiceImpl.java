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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class ItemStatsServiceImpl implements ItemStatsService {

    private static final int PAGE_SIZE = 100;
    private static final ZoneId ZONE_ID = ZoneId.of("UTC");

    private ItemRepository itemRepository;
    private ThreadPoolTaskExecutor itemStatsExecutor;
    private ItemStatsRepository itemStatsRepository;

    private int requestsCount;
    private int pageNumber;
    private List<ItemStats> itemStats;
    private Iterator<ItemStats> iterator;

    Date startOfToday;

    @Autowired
    public ItemStatsServiceImpl(ItemRepository itemRepository, ThreadPoolTaskExecutor itemStatsExecutor, ItemStatsRepository itemStatsRepository) {
        this.itemRepository = itemRepository;
        this.itemStatsExecutor = itemStatsExecutor;
        this.itemStatsRepository = itemStatsRepository;

        this.requestsCount = itemStatsExecutor.getMaxPoolSize();
        setStartOfDay();
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

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    private void setStartOfDay() {
        startOfToday = Date.from(ZonedDateTime.ofInstant(Instant.now(), ZONE_ID).toLocalDate().atStartOfDay(ZONE_ID).toInstant());
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
                updateHighestBuyOrder(jsonObject);
                updateLowestSellOrder(jsonObject);
                updateSoldYesterday();

                updateLastUpdate();
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

        private void updateHighestBuyOrder(JSONObject jsonObject) {
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

            stats.setHighestBuyOrder(highestBuyOrder);
        }

        private void updateLowestSellOrder(JSONObject jsonObject) {
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

            stats.setLowestSellOrder(lowestSellOrder);
        }

        private void updateLastUpdate() {
            stats.setLastUpdate(Date.from(ZonedDateTime.now(ZONE_ID).toInstant()));
        }

        private void updateSoldYesterday() {
            if (stats.getLastUpdate() == null || stats.getLastUpdate().before(startOfToday)) {
                String url = buildPriceOverviewUrl();
                try {
                    String data = Jsoup.connect(url).ignoreContentType(true).execute().body();

                    JSONObject json = new JSONObject(data);
                    stats.setSoldYesterday(Integer.parseInt(((String) json.get("volume")).replaceAll(",", "")));
                } catch (IOException e) {
                    log.error("Could not update sold items count yesterday", e);
                } catch (NumberFormatException e) {
                    log.error("Volume isn't integer", e);
                }
            }
        }

        private String buildPriceOverviewUrl() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("https://steamcommunity.com/market/priceoverview/?currency=1&appid=");
            stringBuilder.append(stats.getId().getGameId());
            stringBuilder.append("&market_hash_name=");
            stringBuilder.append(stats.getItem().getItemName());

            return stringBuilder.toString();
        }

    }

}
