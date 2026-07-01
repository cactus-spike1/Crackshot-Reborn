package fun.cactus.utils.prices;

import java.util.HashMap;
import java.util.Map;

public class PriceManager {

    private final Map<String, Price> prices = new HashMap<>();

    public void register(String weapon, Price price) {
        prices.put(weapon, price);
    }

    public Price get(String weapon) {
        return prices.get(weapon);
    }
}