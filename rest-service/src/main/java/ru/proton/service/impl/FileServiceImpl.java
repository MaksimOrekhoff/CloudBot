package ru.proton.service.impl;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import ru.proton.dao.AppDocumentDao;
import ru.proton.dao.AppPhotoDao;
import ru.proton.entity.AppDocument;
import ru.proton.entity.AppPhoto;
import ru.proton.entity.BinaryContent;
import ru.proton.service.FileService;

import java.io.File;
import java.io.IOException;

@Service
@Log4j
public class FileServiceImpl implements FileService {
    private final AppDocumentDao appDocumentDao;
    private final AppPhotoDao appPhotoDao;

    public FileServiceImpl(AppDocumentDao appDocumentDao, AppPhotoDao appPhotoDao) {
        this.appDocumentDao = appDocumentDao;
        this.appPhotoDao = appPhotoDao;
    }

    @Override
    public AppDocument getDoc(String idDoc) {
        //TODO add дешифратор хэш-строки
        Long id = Long.parseLong(idDoc);
        return appDocumentDao.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String idPhoto) {
        //TODO add дешифратор хэш-строки
        Long id = Long.parseLong(idPhoto);
        return appPhotoDao.findById(id).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            //TODO добавить генерацию имени временного файла
            File temp = File.createTempFile("tempFile", ".bin");
            temp.deleteOnExit();
            FileUtils.writeByteArrayToFile(temp, binaryContent.getFileAsArrayOfBytes());
            return new FileSystemResource(temp);
        } catch (IOException exception) {
            log.error(exception);
            return null;
        }
    }
}
