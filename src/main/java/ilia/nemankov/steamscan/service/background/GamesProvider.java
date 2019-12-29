package ilia.nemankov.steamscan.service.background;

import java.util.List;

public interface GamesProvider {

    List<Game> getGames();

    void updateGames();

}
