package com.vcasino.clicker.mock;

import com.vcasino.clicker.entity.Level;

import java.util.Map;

public class LevelMocks {

    public static Level getLevelMock(Integer level) {
        return getLevels().get(level);
    }

    public static Map<Integer, Level> getLevels() {
        return Map.of(
                1, new Level(1, "Bronze", 0L, 1, 1, 1000),
                2, new Level(2, "Silver", 10000L, 2, 2, 2000),
                3, new Level(3, "Gold", 25000L, 3, 3, 3000),
                4, new Level(4, "Platinum", 100000L, 4, 4, 4000),
                5, new Level(5, "Diamond", 1000000L, 5, 5, 5000),
                6, new Level(6, "Epic", 2000000L, 6, 6, 6000),
                7, new Level(7, "Legendary", 10000000L, 7, 7, 7000),
                8, new Level(8, "Master", 50000000L, 8, 8, 8000),
                9, new Level(9, "Grandmaster", 100000000L, 9, 9, 9000),
                10, new Level(10, "Immortal", 1000000000L, 10, 10, 10000)
        );
    }

}
