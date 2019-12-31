package ilia.nemankov.steamscan.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class UrlUtil {

    public Map<String, String> getUrlParams(String url) {
        try {
            String params = url.split("\\?")[1];
            Map<String, String> result = new HashMap<>();

            String[] pairs = params.split("&");
            for (String pair : pairs) {
                String[] splitPair = pair.split("=");
                if (splitPair.length != 2) {
                    log.warn("Not valid key-value pair: {}", pair);
                    continue;
                }
                result.put(splitPair[0], splitPair[1]);
            }

            return result;
        } catch (IndexOutOfBoundsException e) {
            return Collections.emptyMap();
        }
    }

}
