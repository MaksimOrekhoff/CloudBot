package ru.proton.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.proton.entity.AppDocument;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
}
