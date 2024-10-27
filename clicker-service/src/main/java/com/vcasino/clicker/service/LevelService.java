package com.vcasino.clicker.service;

import com.vcasino.clicker.dto.LevelDto;
import com.vcasino.clicker.entity.Level;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mapper.LevelMapper;
import com.vcasino.clicker.repository.LevelRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class LevelService {

    private List<Level> levels;
    private List<LevelDto> levelDtos;
    private final Map<Integer, Level> levelMap = new HashMap<>();
    private final LevelRepository levelRepository;
    private final LevelMapper levelMapper;

    public Level getLevelAccordingNetWorth(Long netWorth) {
        for (int i = levels.size() - 1; i >= 0; i--) {
            Level level = levels.get(i);
            if (netWorth >= level.getNetWorth()) {
                return level;
            }
        }

        throw new AppException("Level not found for net worth: " + netWorth, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Level getLevelAccordingNetWorth(BigDecimal netWorth) {
        return getLevelAccordingNetWorth(netWorth.longValue());
    }

    public List<LevelDto> getLevels() {
        return levelDtos;
    }

    public Level getLevel(Integer level) {
        return levelMap.get(level);
    }

    @PostConstruct
    private void cacheLevels() {
        levels = levelRepository.findAll();
        levels.sort(Comparator.comparingInt(Level::getValue));
        levelDtos = levelMapper.toDtos(levels);
        levels.forEach(l -> levelMap.put(l.getValue(), l));
        log.info("Cached {} levels, {} levelDtos", levels.size(), levelDtos.size());
    }

}
