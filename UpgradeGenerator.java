import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpgradeGenerator {
    private static final Map<Integer, Integer> paybackMap = Map.of(
            0, 168,
            1, 168,
            2, 168,
            3, 168,
            4, 168,
            5, 168,
            6, 240,
            7, 240,
            8, 336,
            9, 336);

    public static void main(String[] args) {
        generateUpgradeSQL("Telegram", "Social", 6000, 10);
    }

    public static void generateUpgradeSQL(String name, String section, Integer maxProfitPerHour, Integer maxLevel) {
        List<Upgrade> upgrades = generateUpgrades(name, section, maxProfitPerHour, maxLevel);

        System.out.println();

        int totalPriceOfUpgrades = 0;
        int totalProfit = 0;
        for (Upgrade upgrade : upgrades) {
            totalPriceOfUpgrades += upgrade.priceToUpgrade == null ? 0 : upgrade.priceToUpgrade;
            totalProfit += upgrade.profitPerHourDelta == null ? 0 : upgrade.profitPerHourDelta;
        }

        System.out.println("Total price of upgrades: " + totalPriceOfUpgrades);
        System.out.println("Delta profit sum: " + totalProfit);

        System.out.println();

        printSQL(upgrades);
    }

    public static List<Upgrade> generateUpgrades(String name, String section, Integer maxProfitPerHour, Integer maxLevel) {
        List<Upgrade> res = new ArrayList<>();

        double profitScalingFactor = maxProfitPerHour / Math.pow(maxLevel, 2);

        for (int level = 0; level <= maxLevel; level++) {
            int profitPerHour = level == 0 ? 0 : (int) (profitScalingFactor * Math.pow(level, 2));
            int profitPerHourDelta = (int) (profitScalingFactor * Math.pow(level + 1, 2) - profitPerHour);
            Integer hoursToPayback = paybackMap.getOrDefault(level, 0);
            int priceToUpgrade = profitPerHourDelta * hoursToPayback;

            if (level != maxLevel) {
                res.add(new Upgrade(name, section, level, profitPerHour, profitPerHourDelta, priceToUpgrade, "NULL", "FALSE"));
            } else {
                res.add(new Upgrade(name, section, maxLevel, profitPerHour, null, null, null, "TRUE"));
            }
        }

        return res;
    }

    public static void printSQL(List<Upgrade> upgrades) {
        StringBuilder s = new StringBuilder("INSERT INTO upgrade (name, section, level, profit_per_hour, profit_per_hour_delta, price_to_upgrade, condition_id, max_level)\nVALUES ");
        for (Upgrade upgrade : upgrades) {
            if (upgrade.level == 10) {
                s.append(upgrade).append(";");
            } else {
                s.append(upgrade).append(",\n       ");
            }
        }
        System.out.println(s);
    }

    static class Upgrade {
        String name;
        String section;
        Integer level;
        Integer profitPerHour;
        Integer profitPerHourDelta;
        Integer priceToUpgrade;
        String conditionId;
        String maxLevel;

        public Upgrade(String name, String section, Integer level, Integer profitPerHour, Integer profitPerHourDelta, Integer priceToUpgrade, String conditionId, String maxLevel) {
            this.name = name;
            this.section = section;
            this.level = level;
            this.profitPerHour = profitPerHour;
            this.profitPerHourDelta = profitPerHourDelta;
            this.priceToUpgrade = priceToUpgrade;
            this.conditionId = conditionId;
            this.maxLevel = maxLevel;
        }

        @Override
        public String toString() {
            return "(" +
                    "'%s'".formatted(name) + ", " +
                    "'%s'".formatted(section) + ", " +
                    level + ", " +
                    profitPerHour + ", " +
                    (profitPerHourDelta == null ? "NULL" : profitPerHourDelta) + ", " +
                    (priceToUpgrade == null ? "NULL" : priceToUpgrade) + ", " +
                    "NULL" + ", " +
                    maxLevel + ")";
        }
    }
}
