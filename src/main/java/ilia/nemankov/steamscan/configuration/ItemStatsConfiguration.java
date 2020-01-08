package ilia.nemankov.steamscan.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.List;

@Configuration
@PropertySource("classpath:item_stats.properties")
public class ItemStatsConfiguration {

    @Value("${core.pool.size}")
    private int corePoolSize;
    @Value("${max.pool.size}")
    private int maxPoolSize;
    @Value("${queue.capacity}")
    private int queueCapacity;

    @Value("${page.size}")
    private int pageSize;
    @Value("${commission}")
    private double commission;

    @Bean(name = "itemStatsExecutor")
    public ThreadPoolTaskExecutor getTaskExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(corePoolSize);
        threadPool.setMaxPoolSize(maxPoolSize);
        threadPool.setQueueCapacity(queueCapacity);
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        return threadPool;
    }

    @Bean
    @Qualifier("pageSize")
    public Integer getPageSize() {
        return pageSize;
    }

    @Bean
    @Qualifier("commission")
    public Double getCommission() {
        return commission;
    }

}
