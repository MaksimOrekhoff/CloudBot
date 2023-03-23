package ru.proton.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.proton.entity.AppDocument;
import ru.proton.entity.AppPhoto;

public interface FileService {
    AppDocument processDoc(Message message);
    AppPhoto processPhoto(Message message);
}
