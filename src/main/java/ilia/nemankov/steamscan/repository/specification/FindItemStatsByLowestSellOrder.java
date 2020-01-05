package ilia.nemankov.steamscan.repository.specification;

import ilia.nemankov.steamscan.model.ItemStats;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@AllArgsConstructor
public class FindItemStatsByLowestSellOrder implements Specification<ItemStats> {

    private double lowestSellOrder;
    private Sign sign;

    @Override
    public Predicate toPredicate(Root<ItemStats> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        switch (sign) {
            case LESS_THAN:
                return criteriaBuilder.lessThan(root.get("lowestSellOrder"), lowestSellOrder);
            case LESS_THAN_OR_EQUAL:
                return criteriaBuilder.lessThanOrEqualTo(root.get("lowestSellOrder"), lowestSellOrder);
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(root.get("lowestSellOrder"), lowestSellOrder);
            case GREATER_THAN_OR_EQUAL:
                return criteriaBuilder.greaterThanOrEqualTo(root.get("lowestSellOrder"), lowestSellOrder);
            default:
                throw new IllegalArgumentException("Sign can not be null");
        }
    }

}
