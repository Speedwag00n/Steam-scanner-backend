package ilia.nemankov.steamscan.repository.specification;

import ilia.nemankov.steamscan.model.Game;
import ilia.nemankov.steamscan.model.ItemStats;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class FindItemStatsByGameId implements Specification<ItemStats> {

    private Long[] gamesIds;

    @Override
    public Predicate toPredicate(Root<ItemStats> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        if (gamesIds.length == 0) {
            throw new IllegalArgumentException();
        }
        List<Predicate> predicates = new ArrayList<>();
        for (long gameId : gamesIds) {
            Game game = new Game();
            game.setId(gameId);
            predicates.add(criteriaBuilder.equal(root.get("game"), game));
        }
        return criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
    }

}
