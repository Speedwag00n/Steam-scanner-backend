package ilia.nemankov.steamscan.service.background;

import ilia.nemankov.steamscan.model.Game;
import ilia.nemankov.steamscan.repository.GameRepository;
import ilia.nemankov.steamscan.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GameServiceImpl implements GameService {

    private static final String MARKETPLACE_URL = "https://steamcommunity.com/market/";

    private List<Game> games;

    private GameRepository gameRepository;
    private UrlUtil urlUtil;
    private List<Long> scannedGames;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository, UrlUtil urlUtil, List<Long> scannedGames) {
        this.gameRepository = gameRepository;
        this.urlUtil = urlUtil;
        this.scannedGames = scannedGames;
    }

    @PostConstruct
    public void init() {
        List<Game> savedGames;
        if (scannedGames.isEmpty()) {
            savedGames = gameRepository.findAll();
        } else {
            savedGames = gameRepository.findAllByIdIn(scannedGames);
        }
        if (scannedGames.isEmpty() || savedGames.size() == scannedGames.size()) {
            games = savedGames;
        } else {
            log.debug("Started games search");
            games = new ArrayList<>();
            try {
                Document document = Jsoup.connect(MARKETPLACE_URL).get();
                Elements elements = document.body().getElementsByClass("game_button");
                for (Element element : elements) {
                    Game entity = new Game();

                    String gameName = element.child(0).child(0).child(0).attr("alt"); //a:class=game_button -> span:class=game_button_contents -> span:class=game_button_game_icon -> img
                    String gameUrl = element.attr("href");
                    long id = Long.parseLong(urlUtil.getUrlParams(gameUrl).get("appid"));

                    entity.setId(id);
                    entity.setName(gameName);
                    gameRepository.save(entity);
                    if (scannedGames.contains(id) || scannedGames.isEmpty()) {
                        games.add(entity);
                    }
                }
            } catch (IOException e) {
                log.error("Could not open connection for URL \"{}\"", MARKETPLACE_URL, e);
            } catch (NumberFormatException e) {
                log.error("Appid isn't integer", e);
            }
            log.debug("Finished games search");
        }
        if (scannedGames.isEmpty()) {
            log.debug("Search all games. Found {} games", games.size());
        } else {
            log.debug("Search {} games. Found {} games", scannedGames.size(), games.size());
        }
    }

    @Override
    public List<Game> getGames() {
        return Collections.unmodifiableList(games);
    }

}
