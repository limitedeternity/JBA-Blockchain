import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrismaShop {
    private static final String serviceId = "dSJxhS6faBIZPY0";
    private static final String signature =
            "UHJpc21hU2hvcA==#ZWM1MjU3ODg1NDQ2YmUzYjdiZDQxMzI3MGIxYTJkZGFlOWVlOGQxZjM5ZjljZGMyOGY5OGI1ZjE0ZTA4MWMwZA==";

    private final Blockchain instance;
    public enum Products {
        SANDWICH(15, "Sandwich");
        private final int price;
        private final String name;

        Products(int price, String name) {
            this.price = price;
            this.name = name;
        }

        public int getPrice() {
            return price;
        }

        public String getName() {
            return name;
        }
    }

    PrismaShop(Blockchain bc) {
        instance = bc;
    }

    void sellFood(Products product) {
        long customer = Thread.currentThread().getId();
        long incomes = instance.getChain().stream()
                .filter(block -> block.getMinerId() == customer)
                .mapToLong(block -> 100)
                .sum();

        long expenses = instance.getChain().stream()
                .flatMap(block -> block.getBlockData().values().stream().flatMap(Collection::stream))
                .map(hm -> hm.get("message"))
                .mapToLong(message -> {
                    String boughtRegex = "^(\\d+) bought (.+) for (\\d+) VC$";
                    Pattern pattern = Pattern.compile(boughtRegex);
                    Matcher matcher = pattern.matcher(message);
                    if (!matcher.matches() || Long.parseLong(matcher.group(1)) != customer) {
                        return 0L;
                    }

                    return Long.parseLong(matcher.group(3));
                })
                .sum();

        if (incomes - expenses >= product.getPrice()) {
            instance.acceptServiceData(serviceId, signature,
                    customer + " bought " + product.getName() + " for " + product.getPrice() + " VC");
        }
    }
}
