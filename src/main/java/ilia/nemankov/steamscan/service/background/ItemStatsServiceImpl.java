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

    private ItemRepository itemRepository;
    private ThreadPoolTaskExecutor itemStatsExecutor;
    private ItemStatsRepository itemStatsRepository;
    private List<Long> scannedGames;
    private ProxyManager proxyManager;

    private int pageSize;
    private double commission;

    private int requestsCount;
    private int pageNumber;
    private List<ItemStats> itemStats;
    private Iterator<ItemStats> iterator;
    private AtomicBoolean changeProxy = new AtomicBoolean(false);
    private int proxyRequestCount;

    private Proxy currentProxy;

    @Autowired
    public ItemStatsServiceImpl(ItemRepository itemRepository, ThreadPoolTaskExecutor itemStatsExecutor,
                                ItemStatsRepository itemStatsRepository, List<Long> scannedGames,
                                @Qualifier("pageSize") Integer pageSize, Double commission,
                                @Qualifier("itemStatsProxyManager") ProxyManager proxyManager) {
        this.itemRepository = itemRepository;
        this.itemStatsExecutor = itemStatsExecutor;
        this.itemStatsRepository = itemStatsRepository;
        this.scannedGames = scannedGames;
        this.proxyManager = proxyManager;

        this.pageSize = pageSize;
        this.commission = commission;

        this.requestsCount = itemStatsExecutor.getMaxPoolSize();
        if (this.proxyManager.hasProxies()) {
            this.currentProxy = proxyManager.getProxy();
        }
        updateCachedStats();
    }

    @Scheduled(fixedDelay = 5 * 1000)
    private void updateItemStats() {
        if (changeProxy.get()) {
            synchronized (currentProxy) {
                if (this.proxyManager.hasProxies()) {
                    log.debug("Start change proxy. Current proxy: {}, {}. It made {} requests", this.currentProxy.getAddress(), this.currentProxy.getPort(), this.proxyRequestCount);
                    this.currentProxy = proxyManager.getProxy();
                    this.proxyRequestCount = 0;
                    changeProxy.set(false);
                    log.debug("Proxy changed. New proxy: {}, {}", this.currentProxy.getAddress(), this.currentProxy.getPort());
                }
            }
        }
        for (int i = 0; i < requestsCount; i++) {
            if (iterator.hasNext()) {
                try {
                    itemStatsExecutor.execute(new UpdateItemStats(iterator.next(), currentProxy));
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
        if (scannedGames == null || scannedGames.size() == 0) {
            itemStats = itemStatsRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by("itemId"))).getContent();
        } else {
            itemStats = itemStatsRepository.findAllByGameIdIn(scannedGames, PageRequest.of(pageNumber, pageSize, Sort.by("itemId"))).getContent();
        }
        iterator = itemStats.iterator();
        pageNumber++;
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
                synchronized (currentProxy) {
                    if (currentProxy.equals(proxy)) {
                        changeProxy.set(true);
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
            if (proxyManager.hasProxies()) {
                data = Jsoup.connect(url).proxy(proxy.getAddress(), proxy.getPort()).ignoreContentType(true).execute().body();
                proxyRequestCount++;
            } else {
                data = Jsoup.connect(url).ignoreContentType(true).execute().body();
            }

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

        private void updateProfit() {
            double highestBuyOrder = stats.getHighestBuyOrder();
            double lowestSellOrder = stats.getLowestSellOrder();
            if (highestBuyOrder != 0 && lowestSellOrder != 0) {
                double profitAbsolute = lowestSellOrder * (1 - commission) - highestBuyOrder;
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
