package ilia.nemankov.steamscan.service.background;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsManager {

    @Autowired
    private ThreadPoolTaskExecutor analyticsThreadPool;

    @Scheduled(fixedDelay = 5000)
    public void manageThreads() {
        if (analyticsThreadPool.getActiveCount() == analyticsThreadPool.getCorePoolSize()) {
            return;
        }

        //TODO get info from GamesHandler
    }

}
