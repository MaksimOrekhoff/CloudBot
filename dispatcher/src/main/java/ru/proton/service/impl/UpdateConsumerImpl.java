package ru.proton.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.proton.service.UpdateConsumer;

@Service
@Log4j
public class UpdateConsumerImpl implements UpdateConsumer {
    @Override
    public void consume(SendMessage sendMessage) {

    }
}
