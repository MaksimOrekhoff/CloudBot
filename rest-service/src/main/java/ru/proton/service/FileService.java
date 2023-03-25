package ru.proton.service;

import org.springframework.core.io.FileSystemResource;
import ru.proton.entity.AppDocument;
import ru.proton.entity.AppPhoto;
import ru.proton.entity.BinaryContent;

public interface FileService {
    AppDocument getDoc(String idDoc);
    AppPhoto getPhoto(String idPhoto);
    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
