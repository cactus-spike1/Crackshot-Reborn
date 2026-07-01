package fun.cactus.utils.prices;

public class PriceParser {
      public static Price parse(String value) {

        if (value == null || value.isEmpty()) {
            return null;
        }

        String[] parts = value.split("-");

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Price format must be CURRENCY-AMOUNT"
            );
        }

        try {

            String currency = parts[0];
            int amount = Integer.parseInt(parts[1]);

            return new Price(currency, amount);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Invalid amount in price: " + value
            );
        }
    }
}
