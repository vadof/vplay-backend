package com.vcasino.clicker.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.kafka.listener.UserListener;
import com.vcasino.clicker.kafka.message.UserCreate;
import com.vcasino.clicker.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link UserListener}
 */
@ExtendWith(MockitoExtension.class)
public class UserListenerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserListener userListener;

    @Test
    void handleValidJson() throws JsonProcessingException {
        String validJsonData = "{\"userId\": 123}";
        Long expectedUserId = 123L;
        when(objectMapper.readValue(validJsonData, UserCreate.class)).thenReturn(new UserCreate(123L));
        userListener.handle(validJsonData);
        verify(accountService, times(1)).createAccount(expectedUserId);
    }

    @Test
    void handleInvalidJson() throws JsonProcessingException {
        String invalidJsonData = "{\"userId\": \"wrong\"}";
        when(objectMapper.readValue(invalidJsonData, UserCreate.class)).thenThrow(new JsonProcessingException("Invalid JSON") {});
        assertThrows(RuntimeException.class, () -> userListener.handle(invalidJsonData));
        verify(accountService, never()).createAccount(any());
    }

}
