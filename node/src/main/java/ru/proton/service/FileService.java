package ru.proton.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.proton.entity.AppDocument;
import ru.proton.entity.AppPhoto;
import ru.proton.service.enums.LinkType;

public interface FileService {
    AppDocument processDoc(Message message);
    AppPhoto processPhoto(Message message);
    String generateLink(Long docId, LinkType linkType);
}
