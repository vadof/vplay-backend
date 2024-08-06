package com.vcasino.clicker.mock;

import com.vcasino.clicker.entity.Level;

import java.util.ArrayList;
import java.util.List;

public class LevelMocks {

    public static Level getLevelMock() {
        return Level.builder()
                .name("Bronze")
                .netWorth(0L)
                .value(1)
                .build();
    }

    public static List<Level> getLevelMocks() {
        List<Level> levels = new ArrayList<>();
        levels.add(new Level(1,"Bronze",0L));
        levels.add(new Level(2,"Silver",10000L));
        levels.add(new Level(3,"Gold",25000L));
        levels.add(new Level(4,"Platinum",100000L));
        levels.add(new Level(5,"Diamond",1000000L));
        levels.add(new Level(6,"Epic",2000000L));
        levels.add(new Level(7,"Legendary",10000000L));
        levels.add(new Level(8,"Master",50000000L));
        levels.add(new Level(9,"Grandmaster",100000000L));
        levels.add(new Level(10,"Immortal",1000000000L));
        return levels;
    }

}
