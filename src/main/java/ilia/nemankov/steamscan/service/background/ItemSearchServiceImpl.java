package ilia.nemankov.steamscan.service.background;

import ilia.nemankov.steamscan.model.*;
import ilia.nemankov.steamscan.repository.ItemRepository;
import ilia.nemankov.steamscan.repository.ItemStatsRepository;
import ilia.nemankov.steamscan.util.UrlUtil;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemSearchServiceImpl implements ItemSearchService {

    private GameService gameService;
    private UrlUtil urlUtil;
    private ItemRepository itemRepository;
    private ItemStatsRepository itemStatsRepository;

    private List<Game> games;
    private Iterator<Game> iterator;
    private Game currentGame;
    private int nextItemNumber;
    private int scannedItemsCount;

    @Autowired
    public ItemSearchServiceImpl(GameService gameService, UrlUtil urlUtil,
                                 ItemRepository itemRepository, ItemStatsRepository itemStatsRepository,
                                 @Qualifier("scannedItemsCount") Integer scannedItemsCount) {
        this.gameService = gameService;
        this.urlUtil = urlUtil;
        this.itemRepository = itemRepository;
        this.itemStatsRepository = itemStatsRepository;

        this.games = gameService.getGames();
        this.iterator = games.iterator();
        this.currentGame = iterator.next();
        this.scannedItemsCount = scannedItemsCount;
    }

    @Scheduled(fixedDelay = 300 * 1000)
    public void searchItems() {
        try {
            boolean hasNext = analysePage(currentGame, nextItemNumber);
            if (!hasNext) {
                selectNextGame();
                return;
            } else {
                nextItemNumber += scannedItemsCount;
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
        for (Element element : elements) {
            String itemUrl = element.attr("href");
            List<String> nodes = urlUtil.getUrlNodes(itemUrl);
            String itemName = nodes.get(nodes.size() - 1);

            itemNames.add(itemName);
        }

        Set<String> knownItems = itemRepository
                .findByGameAndItemNameIn(currentGame, itemNames)
                .parallelStream()
                .map(item -> item.getItemName())
                .collect(Collectors.toSet());

        itemNames.removeAll(knownItems);

        List<String> itemUrls = new ArrayList<>();
        for (String itemName : itemNames) {
            String itemUrl = buildItemUrl(currentGame, itemName);
            itemUrls.add(itemUrl);
        }

        for (String itemUrl : itemUrls) {
            while (true) {
                try {
                    findAndSaveItem(game, itemUrl);
                    break;
                } catch (HttpStatusException e) {
                    //TODO get new proxy address from pool
                }
            }
        }
        return totalItemsCount > firstItem + scannedItemsCount;
    }

    private void findAndSaveItem(Game game, String itemUrl) throws IOException {
        Document document = Jsoup.connect(itemUrl).get();
        String script = document.getElementsByTag("script").last().data();

        BufferedReader bufferedReader = new BufferedReader(new StringReader(script));
        String line;
        Long itemId = null;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Market_LoadOrderSpread")) {
                try {
                    itemId = Long.parseLong(line.split("\\(")[1].split("\\)")[0].trim());
                    List<String> nodes = urlUtil.getUrlNodes(itemUrl);
                    String itemName = nodes.get(nodes.size() - 1);

                    Item item = new Item();
                    item.setId(new ItemId(itemId, game.getId()));
                    item.setGame(game);
                    item.setItemName(itemName);
                    itemRepository.save(item);

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
        stringBuilder.append("https://steamcommunity.com/market/search/render/?query=&start=");
        stringBuilder.append(firstItem);
        stringBuilder.append("&count=");
        stringBuilder.append(itemsCount);
        stringBuilder.append("&appid=");
        stringBuilder.append(gameId);
        return stringBuilder.toString();
    }

    private String buildItemUrl(Game game, String itemName) {
        return "https://steamcommunity.com/market/listings/" + game.getId() + "/" + itemName;
    }

    private void selectNextGame() {
        if (!iterator.hasNext()) {
            iterator = games.iterator();
        }
        currentGame = iterator.next();
    }

    private ParsedPageInfo getPageInfo(long gameId, long firstItem) throws IOException {
        String url = buildPageUrl(gameId, firstItem, scannedItemsCount);
        String data = null;
        while (true) {
            try {
                data = Jsoup.connect(url).ignoreContentType(true).execute().body();
                break;
            } catch (HttpStatusException e) {
                //TODO get new proxy address from pool
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
