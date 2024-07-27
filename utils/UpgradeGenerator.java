package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpgradeGenerator {
    public static void main(String[] args) {
        Map<Integer, Integer> paybackLevelMap = Map.of(
                0, 0,
                1, 0,
                2, 0,
                3, 0,
                4, 0,
                5, 0,
                6, 0,
                7, 0,
                8, 0,
                9, 0);

        generateUpgradeSQL("AstraZeneca", "Pharmaceutical", 3000, 10, paybackLevelMap, "astrazeneca.sql");
        generateUpgradeSQL("Sanofi", "Pharmaceutical", 6000, 10, paybackLevelMap, "sanofi.sql");
        generateUpgradeSQL("Merck & Co.", "Pharmaceutical", 9000, 10, paybackLevelMap, "merck-co.sql");
        generateUpgradeSQL("Bayer", "Pharmaceutical", 12000, 10, paybackLevelMap, "bayer.sql");
        generateUpgradeSQL("Novartis", "Pharmaceutical", 15000, 10, paybackLevelMap, "novartis.sql");
        generateUpgradeSQL("AbbVie", "Pharmaceutical", 18000, 10, paybackLevelMap, "abbvie.sql");
        generateUpgradeSQL("Roche Holding", "Pharmaceutical", 21000, 10, paybackLevelMap, "roche-holding.sql");
        generateUpgradeSQL("Sinopharm", "Pharmaceutical", 24000, 10, paybackLevelMap, "sinopharm.sql");
        generateUpgradeSQL("Pfizer", "Pharmaceutical", 30000, 10, paybackLevelMap, "pfizer.sql");
        generateUpgradeSQL("Johnson & Johnson", "Pharmaceutical", 62000, 10, paybackLevelMap, "johnson-johnson.sql");
    }

    public static void generateUpgradeSQL(String name, String section, Integer maxProfitPerHour, Integer maxLevel, Map<Integer, Integer> paybackMap, String filePath) {
        List<Upgrade> upgrades = generateUpgrades(name, section, maxProfitPerHour, maxLevel, paybackMap);

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

        String sqlString = getSQL(upgrades);
        System.out.println(sqlString);
        writeStringToFile(filePath, sqlString);
    }

    public static List<Upgrade> generateUpgrades(String name, String section, Integer maxProfitPerHour, Integer maxLevel, Map<Integer, Integer> paybackMap) {
        List<Upgrade> res = new ArrayList<>();

        Double profitScalingFactor = maxProfitPerHour / Math.pow(maxLevel, 2);

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

    public static void writeStringToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSQL(List<Upgrade> upgrades) {
        StringBuilder s = new StringBuilder("INSERT INTO upgrade (name, section, level, profit_per_hour, profit_per_hour_delta, price_to_upgrade, condition_id, max_level)\nVALUES ");
        for (Upgrade upgrade : upgrades) {
            if (upgrade.level == 10) {
                s.append(upgrade).append(";");
            } else {
                s.append(upgrade).append(",\n       ");
            }
        }
        return s.toString();
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
