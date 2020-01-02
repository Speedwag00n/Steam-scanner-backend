package ilia.nemankov.steamscan.service.background;

import ilia.nemankov.steamscan.model.*;
import ilia.nemankov.steamscan.repository.ItemRepository;
import ilia.nemankov.steamscan.repository.ItemSearchCycleRepository;
import ilia.nemankov.steamscan.repository.ItemStatsRepository;
import ilia.nemankov.steamscan.util.UrlUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ItemSearchServiceImpl implements ItemSearchService {

    private static final int ITEMS_COUNT = 20;

    private GameService gameService;
    private ItemSearchCycleRepository cycleRepository;
    private UrlUtil urlUtil;
    private ItemRepository itemRepository;
    private ItemStatsRepository itemStatsRepository;

    private List<Game> games;
    private Iterator<Game> iterator;
    private Game currentGame;

    @Autowired
    public ItemSearchServiceImpl(GameService gameService, ItemSearchCycleRepository cycleRepository, UrlUtil urlUtil, ItemRepository itemRepository, ItemStatsRepository itemStatsRepository) {
        this.gameService = gameService;
        this.cycleRepository = cycleRepository;
        this.urlUtil = urlUtil;
        this.itemRepository = itemRepository;
        this.itemStatsRepository = itemStatsRepository;

        this.games = gameService.getGames();
        this.iterator = games.iterator();
        this.currentGame = iterator.next();
    }

    @Scheduled(fixedDelay = 300 * 1000)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void searchItems() {
        ItemSearchCycle analysedGame = null;
        boolean gameFound = false;
        while (!gameFound) {
            Optional<ItemSearchCycle> analysedGameOptional = cycleRepository.findById(currentGame.getId());
            if (analysedGameOptional.isPresent()) {
                analysedGame = analysedGameOptional.get();
            } else {
                analysedGame = cycleRepository.save(new ItemSearchCycle(currentGame.getId()));
            }
            if (!analysedGame.isSearchFinished()) {
                gameFound = true;
            } else {
                if (iterator.hasNext()) {
                    currentGame = iterator.next();
                } else {
                    startNewCycle();
                }
            }
        }

        try {
            boolean hasNext = analysePage(currentGame, analysedGame.getNextItem());
            if (!hasNext) {
                analysedGame.setSearchFinished(true);
                cycleRepository.save(analysedGame);
                if (!iterator.hasNext()) {
                    startNewCycle();
                }
                return;
            }
        } catch (IOException e) {
            log.warn("Items search failed", e);
        }
        analysedGame.setNextItem(analysedGame.getNextItem() + ITEMS_COUNT);
        cycleRepository.save(analysedGame);
    }

    private boolean analysePage(Game game, int firstItem) throws IOException {
        ParsedPageInfo pageInfo = getPageInfo(game.getId(), firstItem);

        if (pageInfo.elements.isEmpty()) {
            firstItem = pageInfo.getTotalItemsCount() - firstItem;
            if (firstItem > 0) {
                pageInfo = getPageInfo(game.getId(), firstItem);
            } else {
                return false;
            }
        }

        int totalItemsCount = pageInfo.totalItemsCount;
        Elements elements = pageInfo.getElements();

        for (Element element : elements) {
            String itemUrl = element.attr("href");
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
        return totalItemsCount > firstItem + ITEMS_COUNT;
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

    private void startNewCycle() {
        cycleRepository.initNewCycle();
        iterator = games.iterator();
        currentGame = iterator.next();
    }

    private ParsedPageInfo getPageInfo(long gameId, long firstItem) throws IOException {
        String url = buildPageUrl(gameId, firstItem, ITEMS_COUNT);
        String data = Jsoup.connect(url).ignoreContentType(true).execute().body();

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
