package ilia.nemankov.steamscan.configuration.proxy;

import java.util.Iterator;
import java.util.List;

public class ProxyManagerCycled implements ProxyManager {

    private List<Proxy> proxies;
    private Iterator<Proxy> iterator;

    public ProxyManagerCycled(List<Proxy> proxies) {
        this.proxies = proxies;
        if (hasProxies()) {
            this.iterator = proxies.iterator();
        }
    }

    @Override
    public boolean hasProxies() {
        return !this.proxies.isEmpty();
    }

    @Override
    public Proxy getProxy() {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            iterator = proxies.iterator();
            return iterator.next();
        }
    }

}
