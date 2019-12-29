package ilia.nemankov.steamscan.service.background;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class GamesProviderAll implements GamesProvider {

    public GamesProviderAll() {
        games = new ArrayList<>();
        updateGames();
    }

    private static final String marketplaceUrl = "https://steamcommunity.com/market/";

    private List<Game> games;

    @Override
    public List<Game> getGames() {
        return games;
    }

    @Override
    public void updateGames() {
        log.debug("Started games list updating");
        games.clear();
        try {
            Document document = Jsoup.connect(marketplaceUrl).get();
            Elements elements = document.body().getElementsByClass("game_button");

            Iterator<Element> iterator = elements.iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();

                Game game = new Game();

                String gameName = element.child(0).child(0).child(0).attr("alt"); //a:class=game_button -> span:class=game_button_contents -> span:class=game_button_game_icon -> img
                game.setName(gameName);

                game.setGameUrl(element.attr("href"));
            }
        } catch (IOException e) {
            log.error("Could not open connection for URL \"{}\"", marketplaceUrl, e);
        }
        log.debug("Finished games list updating");
    }

}
