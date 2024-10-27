package com.vcasino.clicker.service;

import com.vcasino.clicker.entity.Level;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mock.LevelMocks;
import com.vcasino.clicker.repository.LevelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UNIT tests for {@link LevelService}
 */
@ExtendWith(MockitoExtension.class)
public class LevelServiceTest {
    @Mock
    private LevelRepository levelRepository;

    @InjectMocks
    private LevelService levelService;

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        List<Level> mockLevels = LevelMocks.getLevels().values().
                stream().sorted(Comparator.comparingInt(Level::getValue))
                .toList();

        Field levelsField = LevelService.class.getDeclaredField("levels");
        levelsField.setAccessible(true);
        levelsField.set(levelService, mockLevels);
    }

    @Test
    @DisplayName("Get level according to net worth")
    void getLevelAccordingNetWorth() {
        Level result;

        result = levelService.getLevelAccordingNetWorth(0L);
        assertEquals("Bronze", result.getName());
        result = levelService.getLevelAccordingNetWorth(5000L);
        assertEquals("Bronze", result.getName());
        result = levelService.getLevelAccordingNetWorth(10000L - 1);
        assertEquals("Bronze", result.getName());
        assertEquals(0L, result.getNetWorth());

        result = levelService.getLevelAccordingNetWorth(100000L);
        assertEquals("Platinum", result.getName());
        result = levelService.getLevelAccordingNetWorth(100001L);
        assertEquals("Platinum", result.getName());
        result = levelService.getLevelAccordingNetWorth(1000000L - 100);
        assertEquals("Platinum", result.getName());
        assertEquals(100000, result.getNetWorth());

        result = levelService.getLevelAccordingNetWorth(1000000000L - 1000000);
        assertEquals("Grandmaster", result.getName());

        result = levelService.getLevelAccordingNetWorth(1000000000L);
        assertEquals("Immortal", result.getName());
        result = levelService.getLevelAccordingNetWorth(1000000000L * 10);
        assertEquals("Immortal", result.getName());
        assertEquals(10, result.getValue());

        Exception exception = assertThrows(AppException.class, () -> levelService.getLevelAccordingNetWorth(-500L));

        String expectedMessage = "Level not found for net worth: -500";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
