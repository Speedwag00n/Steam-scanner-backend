package ilia.nemankov.steamscan.service.background;

import ilia.nemankov.steamscan.configuration.proxy.Proxy;
import ilia.nemankov.steamscan.configuration.proxy.ProxyManager;
import ilia.nemankov.steamscan.model.Game;
import ilia.nemankov.steamscan.model.Item;
import ilia.nemankov.steamscan.model.ItemId;
import ilia.nemankov.steamscan.model.ItemStats;
import ilia.nemankov.steamscan.repository.ItemRepository;
import ilia.nemankov.steamscan.repository.ItemStatsRepository;
import ilia.nemankov.steamscan.util.UrlUtil;
import liquibase.pro.packaged.I;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemSearchServiceImpl implements ItemSearchService {

    @Value("${url.listings}")
    private String listingsUrl;
    @Value("${url.item}")
    private String itemUrlBase;

    private GameService gameService;
    private UrlUtil urlUtil;
    private ItemRepository itemRepository;
    private ItemStatsRepository itemStatsRepository;

    private ItemSearchServiceState itemSearchServiceState;

    @Autowired
    public ItemSearchServiceImpl(GameService gameService, UrlUtil urlUtil,
                                 ItemRepository itemRepository, ItemStatsRepository itemStatsRepository,
                                 ItemSearchServiceState itemSearchServiceState) {
        this.gameService = gameService;
        this.urlUtil = urlUtil;
        this.itemRepository = itemRepository;
        this.itemStatsRepository = itemStatsRepository;
        this.itemSearchServiceState = itemSearchServiceState;

        itemSearchServiceState.setGames(gameService.getGames());
    }

    @Scheduled(fixedDelay = 300 * 1000)
    public void searchItems() {
        try {
            boolean hasNext = analysePage(itemSearchServiceState.getCurrentGame(), itemSearchServiceState.getNextItemNumber());

            if (!hasNext) {
                // All pages for the current game was analyzed. Select the next game from the list of games
                itemSearchServiceState.selectNextGame();
            } else {
                // Next page exists. Analyze it
                itemSearchServiceState.nextPage();
            }
        } catch (IOException e) {
            log.warn("Items search failed", e);
        }
    }

    private boolean analysePage(Game game, int firstItem) throws IOException {
        ParsedPageInfo pageInfo = getPageInfo(game.getId(), firstItem);

        if (pageInfo.getElements().isEmpty()) {
            return false;
        }

        int totalItemsCount = pageInfo.getTotalItemsCount();
        Elements elements = pageInfo.getElements();

        Set<String> itemNames = new HashSet<>();

        // Parse names of items from urls
        for (Element element : elements) {
            itemNames.add(parseItemName(element));
        }

        // Find which items are already saved
        Set<String> knownItems = itemRepository
                .findByGameAndItemNameIn(itemSearchServiceState.getCurrentGame(), itemNames)
                .parallelStream()
                .map(item -> item.getItemName())
                .collect(Collectors.toSet());

        itemNames.removeAll(knownItems);

        // Build urls to personal items' pages
        List<String> itemUrls = new ArrayList<>();
        for (String itemName : itemNames) {
            String itemUrl = buildItemUrl(itemSearchServiceState.getCurrentGame(), itemName);
            itemUrls.add(itemUrl);
        }

        for (String itemUrl : itemUrls) {
            while (true) {
                try {
                    findAndSaveItem(game, itemUrl);
                    break;
                } catch (HttpStatusException | SocketException | SocketTimeoutException e) {
                    itemSearchServiceState.changeProxy();
                }
            }
        }
        return totalItemsCount > firstItem + itemSearchServiceState.getScannedItemsCount();
    }

    private String parseItemName(Element element) {
        String itemUrl = element.attr("href");
        List<String> nodes = urlUtil.getUrlNodes(itemUrl);
        return nodes.get(nodes.size() - 1);
    }

    private void findAndSaveItem(Game game, String itemUrl) throws IOException {
        Document document;
        if (itemSearchServiceState.getCurrentProxy() != null) {
            document = Jsoup.connect(itemUrl).proxy(itemSearchServiceState.getCurrentProxy().getAddress(), itemSearchServiceState.getCurrentProxy().getPort()).get();
            itemSearchServiceState.increaseProxyRequestCount();
        } else {
            document = Jsoup.connect(itemUrl).get();
        }
        String script = document.getElementsByTag("script").last().data();

        BufferedReader bufferedReader = new BufferedReader(new StringReader(script));
        String line;
        Long itemId = null;

        // Parse script content
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Market_LoadOrderSpread")) {
                try {
                    itemId = Long.parseLong(line.split("\\(")[1].split("\\)")[0].trim());
                    List<String> nodes = urlUtil.getUrlNodes(itemUrl);
                    String itemName = nodes.get(nodes.size() - 1);

                    // Save info about item
                    Item item = new Item();
                    item.setId(new ItemId(itemId, game.getId()));
                    item.setGame(game);
                    item.setItemName(itemName);
                    itemRepository.save(item);

                    // Save black stats entity
                    ItemStats stats = new ItemStats();
                    stats.setId(item.getId());
                    stats.setGame(game);
                    itemStatsRepository.save(stats);
                    break;
                } catch (IndexOutOfBoundsException e) {
                    log.error("Script contains unexpected code: {}", line, e);
                } catch (NumberFormatException e) {
                    log.error("Item name isn't integer: {}", line, e);
                }
            }
        }
        if (itemId == null) {
            log.error("Item id not found. Script: {}", script);
        }
    }

    private String buildPageUrl(long gameId, long firstItem, long itemsCount) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(itemUrlBase);
        stringBuilder.append(firstItem);
        stringBuilder.append("&count=");
        stringBuilder.append(itemsCount);
        stringBuilder.append("&appid=");
        stringBuilder.append(gameId);
        return stringBuilder.toString();
    }

    private String buildItemUrl(Game game, String itemName) {
        return listingsUrl + game.getId() + "/" + itemName;
    }

    private ParsedPageInfo getPageInfo(long gameId, long firstItem) throws IOException {
        // Parse information about total items count + get all HTML item nodes
        String url = buildPageUrl(gameId, firstItem, itemSearchServiceState.getScannedItemsCount());
        String data = null;

        boolean requestMade = false;

        while (!requestMade) {
            try {
                if (itemSearchServiceState.getProxyManager().hasProxies()) {
                    data = Jsoup.connect(url).proxy(itemSearchServiceState.getCurrentProxy().getAddress(), itemSearchServiceState.getCurrentProxy().getPort()).ignoreContentType(true).execute().body();
                    itemSearchServiceState.increaseProxyRequestCount();
                } else {
                    data = Jsoup.connect(url).ignoreContentType(true).execute().body();
                }

                requestMade = true;
            } catch (HttpStatusException | SocketException | SocketTimeoutException e) {
                itemSearchServiceState.changeProxy();
            }
        }

        JSONObject jsonObject = new JSONObject(data);
        int totalItemCount = (Integer) jsonObject.get("total_count");
        String html = (String) jsonObject.get("results_html");

        Document document = Jsoup.parse(html);
        Elements elements = document.body().getElementsByClass("market_listing_row_link");

        ParsedPageInfo parsedPageInfo = new ParsedPageInfo();
        parsedPageInfo.setTotalItemsCount(totalItemCount);
        parsedPageInfo.setElements(elements);

        return parsedPageInfo;
    }

    @Getter
    @Setter
    private static class ParsedPageInfo {

        private int totalItemsCount;
        private Elements elements;

    }

}
