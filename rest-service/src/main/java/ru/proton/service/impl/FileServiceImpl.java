package ru.proton.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import ru.proton.dao.AppDocumentDao;
import ru.proton.dao.AppPhotoDao;
import ru.proton.entity.AppDocument;
import ru.proton.entity.AppPhoto;
import ru.proton.service.FileService;
import ru.proton.utils.CryptoTool;

@Service
@Log4j
public class FileServiceImpl implements FileService {
    private final AppDocumentDao appDocumentDao;
    private final AppPhotoDao appPhotoDao;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentDao appDocumentDao, AppPhotoDao appPhotoDao, CryptoTool cryptoTool) {
        this.appDocumentDao = appDocumentDao;
        this.appPhotoDao = appPhotoDao;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument getDoc(String hash) {
        Long id = cryptoTool.ofId(hash);
        if (id == null) {
            return null;
        }
        return appDocumentDao.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String hash) {
        Long id = cryptoTool.ofId(hash);
        if (id == null) {
            return null;
        }
        return appPhotoDao.findById(id).orElse(null);
    }
}
