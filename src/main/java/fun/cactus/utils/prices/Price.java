package fun.cactus.utils.prices;

public class Price {

    private final String currency;
    private final int amount;

    public Price(String currency, int amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getAmount() {
        return amount;
    }
}
