package com.vcasino.clicker.service;

import com.vcasino.clicker.entity.Level;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.repository.LevelRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class LevelService {

    private List<Level> levels;

    private final LevelRepository levelRepository;

    public Level getLevelAccordingNetWorth(Long netWorth) {
        log.info("Get level for {} net worth", netWorth);

        for (int i = levels.size() - 1; i >= 0; i--) {
            Level level = levels.get(i);
            if (netWorth >= level.getNetWorth()) {
                return level;
            }
        }

        throw new AppException("Level not found for net worth: " + netWorth, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostConstruct
    private void cacheLevels() {
        List<Level> levels = levelRepository.findAll();
        levels.sort(Comparator.comparingLong(Level::getNetWorth));
        log.info("Cached {} levels", levels.size());
    }

}
