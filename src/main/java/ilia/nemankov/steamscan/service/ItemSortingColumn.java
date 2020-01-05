package ilia.nemankov.steamscan.service;

public enum ItemSortingColumn {

    ID,
    HIGHEST_BUY_ORDER,
    LOWEST_SELL_ORDER,
    LAST__UPDATE,
    PROFIT_ABSOLUTE,
    PROFIT_RELATIVE;

    @Override
    public String toString() {
        switch (this) {
            case ID:
                return "id";
            case HIGHEST_BUY_ORDER:
                return "highestBuyOrder";
            case LOWEST_SELL_ORDER:
                return "lowestSellOrder";
            case LAST__UPDATE:
                return "lastUpdate";
            case PROFIT_ABSOLUTE:
                return "profitAbsolute";
            case PROFIT_RELATIVE:
                return "profitRelative";

            default:
                throw new IllegalArgumentException();
        }
    }

}
