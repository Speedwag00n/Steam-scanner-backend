package ilia.nemankov.steamscan.repository.specification;

import ilia.nemankov.steamscan.model.ItemStats;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@AllArgsConstructor
public class FindItemStatsByProfitRelative implements Specification<ItemStats> {

    private double profitRelative;
    private Sign sign;

    @Override
    public Predicate toPredicate(Root<ItemStats> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        switch (sign) {
            case LESS_THAN:
                return criteriaBuilder.lessThan(root.get("profitRelative"), profitRelative);
            case LESS_THAN_OR_EQUAL:
                return criteriaBuilder.lessThanOrEqualTo(root.get("profitRelative"), profitRelative);
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(root.get("profitRelative"), profitRelative);
            case GREATER_THAN_OR_EQUAL:
                return criteriaBuilder.greaterThanOrEqualTo(root.get("profitRelative"), profitRelative);
            default:
                throw new IllegalArgumentException("Sign can not be null");
        }
    }

}
