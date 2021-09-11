package ilia.nemankov.steamscan.service.background;

import ilia.nemankov.steamscan.configuration.proxy.Proxy;
import ilia.nemankov.steamscan.configuration.proxy.ProxyManager;
import ilia.nemankov.steamscan.model.Game;
import ilia.nemankov.steamscan.model.ItemStats;
import ilia.nemankov.steamscan.repository.ItemStatsRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
@Slf4j
@Component
public class ItemStatsServiceState {

    private int requestsCount;
    private int pageNumber;
    private List<ItemStats> itemStats;
    private Iterator<ItemStats> iterator;
    private AtomicBoolean changeProxy = new AtomicBoolean(false);
    private int proxyRequestCount;
    private List<Long> scannedGames;
    private ProxyManager proxyManager;
    private Proxy currentProxy;

    private Integer pageSize;
    private Double commission;

    private ItemStatsRepository itemStatsRepository;

    public ItemStatsServiceState(@Qualifier("itemStatsProxyManager") ProxyManager proxyManager, ItemStatsRepository itemStatsRepository,
                                 @Qualifier("pageSize") Integer pageSize, Double commission) {
        this.proxyManager = proxyManager;
        if (this.proxyManager.hasProxies()) {
            this.currentProxy = proxyManager.getProxy();
        }

        this.itemStatsRepository = itemStatsRepository;

        this.pageSize = pageSize;
        this.commission = commission;
    }

    public void updateCachedStats() {
        if (scannedGames == null || scannedGames.size() == 0) {
            itemStats = itemStatsRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by("itemId"))).getContent();
        } else {
            itemStats = itemStatsRepository.findAllByGameIdIn(scannedGames, PageRequest.of(pageNumber, pageSize, Sort.by("itemId"))).getContent();
        }
        iterator = itemStats.iterator();
        pageNumber++;
    }

    public void changeProxy() {
        if (this.proxyManager.hasProxies()) {
            log.debug("Start change proxy. Current proxy: {}, {}. It made {} requests", this.currentProxy.getAddress(), this.currentProxy.getPort(), this.proxyRequestCount);
            this.currentProxy = proxyManager.getProxy();
            this.proxyRequestCount = 0;
            changeProxy.set(false);
            log.debug("Proxy changed. New proxy: {}, {}", this.currentProxy.getAddress(), this.currentProxy.getPort());
        }
    }

    public void increaseProxyRequestCount() {
        this.proxyRequestCount += 1;
    }

}
