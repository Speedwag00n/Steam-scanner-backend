package ilia.nemankov.steamscan.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.List;

@Configuration
@PropertySource("classpath:scan.properties")
public class ScanConfiguration {

    @Value("${scanned.games.ids}")
    private Long[] scannedGamesIds;

    @Bean(name = "scannedGames")
    public List<Long> scannedGames() {
        return Arrays.asList(scannedGamesIds);
    }

}

