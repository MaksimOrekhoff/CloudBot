package ru.proton.service;

import ru.proton.entity.AppDocument;
import ru.proton.entity.AppPhoto;

public interface FileService {
    AppDocument getDoc(String idDoc);

    AppPhoto getPhoto(String idPhoto);
}
