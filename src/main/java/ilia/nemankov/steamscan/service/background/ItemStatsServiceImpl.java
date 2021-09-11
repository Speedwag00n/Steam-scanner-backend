package ilia.nemankov.steamscan.service.background;

import ilia.nemankov.steamscan.configuration.proxy.Proxy;
import ilia.nemankov.steamscan.configuration.proxy.ProxyManager;
import ilia.nemankov.steamscan.model.ItemStats;
import ilia.nemankov.steamscan.repository.ItemRepository;
import ilia.nemankov.steamscan.repository.ItemStatsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class ItemStatsServiceImpl implements ItemStatsService {

    @Value("${url.item.stats}")
    private String itemStatsUrl;

    private ItemRepository itemRepository;
    private ThreadPoolTaskExecutor itemStatsExecutor;
    private ItemStatsRepository itemStatsRepository;

    private ItemStatsServiceState itemStatsServiceState;

    @Autowired
    public ItemStatsServiceImpl(ItemRepository itemRepository, ThreadPoolTaskExecutor itemStatsExecutor,
                                ItemStatsRepository itemStatsRepository, List<Long> scannedGames,
                                ItemStatsServiceState itemStatsServiceState) {
        this.itemRepository = itemRepository;
        this.itemStatsExecutor = itemStatsExecutor;
        this.itemStatsRepository = itemStatsRepository;

        this.itemStatsServiceState = itemStatsServiceState;
        itemStatsServiceState.setScannedGames(scannedGames);
        itemStatsServiceState.setRequestsCount(itemStatsExecutor.getMaxPoolSize());

        // Save some stats from db in memory
        itemStatsServiceState.updateCachedStats();
    }

    @Scheduled(fixedDelay = 5 * 1000)
    private void updateItemStats() {
        if (itemStatsServiceState.getChangeProxy().get()) {
            synchronized (itemStatsServiceState) {
                itemStatsServiceState.changeProxy();
            }
        }

        // Fill pull of executors
        for (int i = 0; i < itemStatsServiceState.getProxyRequestCount(); i++) {
            if (itemStatsServiceState.getIterator().hasNext()) {
                try {
                    itemStatsExecutor.execute(new UpdateItemStats(itemStatsServiceState.getIterator().next(), itemStatsServiceState.getCurrentProxy()));
                } catch (TaskRejectedException e) {
                    log.warn("Queue is full", e);
                    return;
                }
            } else {
                itemStatsServiceState.updateCachedStats();
                if (itemStatsServiceState.getItemStats().isEmpty()) {
                    itemStatsServiceState.setPageNumber(0);
                    return;
                }
            }
        }
    }

    @AllArgsConstructor
    class UpdateItemStats implements Runnable {

        private ItemStats stats;
        private Proxy proxy;

        @Override
        public void run() {
            JSONObject jsonObject;
            try {
                jsonObject = getStats();
            } catch (HttpStatusException | SocketException | SocketTimeoutException e) {
                synchronized (itemStatsServiceState) {
                    if (itemStatsServiceState.getCurrentProxy().equals(proxy)) {
                        itemStatsServiceState.getChangeProxy().set(true);
                    }
                }
                return;
            } catch (IOException e) {
                log.warn("Could not get item stats with id {}", stats.getId().getItemId(), e);
                return;
            }

            try {
                updateHighestBuyOrder(jsonObject);
                updateLowestSellOrder(jsonObject);
                updateProfit();

                updateLastUpdate();
            } catch (Exception e) {
                log.warn("Unexpected error during stats updating happened", e);
                return;
            }

            itemStatsRepository.save(stats);
        }

        private JSONObject getStats() throws IOException {
            String url = buildStatsUrl();
            String data;
            if (itemStatsServiceState.getProxyManager().hasProxies()) {
                data = Jsoup.connect(url).proxy(proxy.getAddress(), proxy.getPort()).ignoreContentType(true).execute().body();
                itemStatsServiceState.increaseProxyRequestCount();
            } else {
                data = Jsoup.connect(url).ignoreContentType(true).execute().body();
            }

            return new JSONObject(data);
        }

        private String buildStatsUrl() {
            return itemStatsUrl + stats.getId().getItemId();
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

        private void updateProfit() {
            double highestBuyOrder = stats.getHighestBuyOrder();
            double lowestSellOrder = stats.getLowestSellOrder();
            if (highestBuyOrder != 0 && lowestSellOrder != 0) {
                double profitAbsolute = lowestSellOrder * (1 - itemStatsServiceState.getCommission()) - highestBuyOrder;
                double profitRelative = profitAbsolute / highestBuyOrder;

                stats.setProfitAbsolute(profitAbsolute);
                stats.setProfitRelative(profitRelative);
            }
        }

        private void updateLastUpdate() {
            stats.setLastUpdate(new Date());
        }

    }

}
