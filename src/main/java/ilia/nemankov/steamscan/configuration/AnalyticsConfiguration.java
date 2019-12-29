package ilia.nemankov.steamscan.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@PropertySource("classpath:analytics.properties")
public class AnalyticsConfiguration {

    @Value("${core.pool.size}")
    private String corePoolSize;
    @Value("${max.pool.size}")
    private String maxPoolSize;

    @Bean(name = "analyticsThreadPool")
    public ThreadPoolTaskExecutor getTaskExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(Integer.parseInt(corePoolSize));
        threadPool.setMaxPoolSize(Integer.parseInt(maxPoolSize));
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        return threadPool;
    }

}
