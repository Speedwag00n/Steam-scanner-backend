package ilia.nemankov.steamscan.repository.specification;

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
public class FindItemStatsByHighestBuyOrder implements Specification<ItemStats> {

    private double highestBuyOrder;
    private Sign sign;

    @Override
    public Predicate toPredicate(Root<ItemStats> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        switch (sign) {
            case LESS_THAN:
                return criteriaBuilder.lessThan(root.get("highestBuyOrder"), highestBuyOrder);
            case LESS_THAN_OR_EQUAL:
                return criteriaBuilder.lessThanOrEqualTo(root.get("highestBuyOrder"), highestBuyOrder);
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(root.get("highestBuyOrder"), highestBuyOrder);
            case GREATER_THAN_OR_EQUAL:
                return criteriaBuilder.greaterThanOrEqualTo(root.get("highestBuyOrder"), highestBuyOrder);
            default:
                throw new IllegalArgumentException("Sign can not be null");
        }
    }

}
