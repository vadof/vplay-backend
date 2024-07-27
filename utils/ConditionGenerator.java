package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ConditionGenerator {
    public static void main(String[] args) {
        generateCondition("ByUpgrade", "Facebook", 1, 10, "condition/facebook.sql");
        generateCondition("ByUpgrade", "Instagram", 1, 10, "condition/instagram.sql");
        generateCondition("ByUpgrade", "Reddit", 1, 10, "condition/reddit.sql");
        generateCondition("ByUpgrade", "Snapchat", 1, 10, "condition/snapchat.sql");
        generateCondition("ByUpgrade", "Telegram", 1, 10, "condition/telegram.sql");
        generateCondition("ByUpgrade", "TikTok", 1, 10, "condition/tiktok.sql");
        generateCondition("ByUpgrade", "WeChat", 1, 10, "condition/wechat.sql");
        generateCondition("ByUpgrade", "WhatsApp", 1, 10, "condition/whatsapp.sql");
        generateCondition("ByUpgrade", "X", 1, 10, "condition/x.sql");
        generateCondition("ByUpgrade", "YouTube", 1, 10, "condition/youtube.sql");
    }

    public static void generateCondition(String type, String upgradeName, Integer startLevel, Integer maxLevel, String filePath) {
        StringBuilder sb = new StringBuilder("INSERT INTO condition (type, upgrade_name, level)\nVALUES ");
        for (int i = startLevel; i <= maxLevel; i++) {
            if (i == maxLevel) {
                sb.append(new Condition(type, upgradeName, i)).append(";");
            } else {
                sb.append(new Condition(type, upgradeName, i)).append(",\n       ");
            }
        }

        System.out.println();

        String sqlString = sb.toString();

        System.out.println(sqlString);

        writeStringToFile(filePath, sqlString);
    }

    public static void writeStringToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Condition {
        String type;
        String upgradeName;
        Integer level;

        public Condition(String type, String upgradeName, Integer level) {
            this.type = type;
            this.upgradeName = upgradeName;
            this.level = level;
        }

        @Override
        public String toString() {
            return "('" + type + "', " +
                    "'" + upgradeName + "', " +
                    level + ")";
        }
    }
}
