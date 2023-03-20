package ru.proton.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface UpdateConsumer {
    void consume(SendMessage sendMessage);
}
