package ilia.nemankov.steamscan.service.background;

import ilia.nemankov.steamscan.configuration.proxy.Proxy;
import ilia.nemankov.steamscan.configuration.proxy.ProxyManager;
import ilia.nemankov.steamscan.model.Game;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@Slf4j
@Component
public class ItemSearchServiceState {

    private ProxyManager proxyManager;

    private List<Game> games;
    private Iterator<Game> iterator;
    private Game currentGame;
    private int nextItemNumber;
    private int scannedItemsCount;
    private Proxy currentProxy;
    private int proxyRequestCount;

    public ItemSearchServiceState(@Qualifier("itemProxyManager") ProxyManager proxyManager,
                                  @Qualifier("scannedItemsCount") Integer scannedItemsCount) {
        this.proxyManager = proxyManager;
        if (proxyManager.hasProxies()) {
            this.currentProxy = proxyManager.getProxy();
        }
        this.scannedItemsCount = scannedItemsCount;
    }

    public void setGames(List<Game> games) {
        this.games = games;
        iterator = games.iterator();
        currentGame = iterator.next();
    }

    public void nextPage() {
        nextItemNumber += scannedItemsCount;
    }

    public void selectNextGame() {
        if (!iterator.hasNext()) {
            iterator = games.iterator();
        }

        currentGame = iterator.next();
    }

    public void changeProxy() {
        if (this.proxyManager.hasProxies()) {
            log.debug("Start change proxy. Current proxy: {}, {}. It made {} requests", this.currentProxy.getAddress(), this.currentProxy.getPort(), this.proxyRequestCount);
            this.currentProxy = proxyManager.getProxy();
            this.proxyRequestCount = 0;
            log.debug("Proxy changed. New proxy: {}, {}", this.currentProxy.getAddress(), this.currentProxy.getPort());
        }
    }

    public void increaseProxyRequestCount() {
        this.proxyRequestCount += 1;
    }

}
