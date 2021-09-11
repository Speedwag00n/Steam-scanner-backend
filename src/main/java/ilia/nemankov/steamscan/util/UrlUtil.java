package ilia.nemankov.steamscan.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class UrlUtil {

    public Map<String, String> getUrlParams(String url) {
        try {
            // Get part of url after "?". It's only params
            String params = url.split("\\?")[1];
            // Save params as key - value from url
            Map<String, String> result = new HashMap<>();

            // Split params
            String[] pairs = params.split("&");
            for (String pair : pairs) {
                // Get key and value
                String[] splitPair = pair.split("=");
                // Not a key-value string. Ignore it
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

    public List<String> getUrlNodes(String url) {
        // Get nodes from url and remove protocol
        List<String> nodes = Arrays.asList(url.split("/"));
        return nodes.subList(1, nodes.size());
    }

}
