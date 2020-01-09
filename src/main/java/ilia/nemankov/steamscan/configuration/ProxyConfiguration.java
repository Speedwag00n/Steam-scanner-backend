package ilia.nemankov.steamscan.configuration;

import ilia.nemankov.steamscan.configuration.proxy.Proxy;
import ilia.nemankov.steamscan.configuration.proxy.ProxyManager;
import ilia.nemankov.steamscan.configuration.proxy.ProxyManagerCycled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@PropertySource("classpath:proxy.properties")
public class ProxyConfiguration {

    @Value("${proxy.addresses}")
    private String[] proxyInfo;

    @Bean
    public List<Proxy> getProxies() {
        List<Proxy> proxies = new ArrayList<>();
        for (String info : proxyInfo) {
            proxies.add(new Proxy(info));
        }

        return proxies;
    }

    @Bean
    @Qualifier("itemProxyManager")
    public ProxyManager getItemProxyManager() {
        return new ProxyManagerCycled(getProxies());
    }

    @Bean
    @Qualifier("itemStatsProxyManager")
    public ProxyManager getItemStatsProxyManager() {
        return new ProxyManagerCycled(getProxies());
    }

}

