package ru.proton.service.impl;

import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.proton.dao.AppDocumentDao;
import ru.proton.dao.AppPhotoDao;
import ru.proton.dao.BinaryContentDao;
import ru.proton.entity.AppDocument;
import ru.proton.entity.AppPhoto;
import ru.proton.entity.BinaryContent;
import ru.proton.exceptions.UploadFileException;
import ru.proton.service.FileService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Service
@Log4j
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    private final BinaryContentDao binaryContentDAO;
    private final AppDocumentDao appDocumentDAO;
    private final AppPhotoDao appPhotoDao;

    public FileServiceImpl(BinaryContentDao binaryContentDAO, AppDocumentDao appDocumentDAO, AppPhotoDao appPhotoDao) {
        this.binaryContentDAO = binaryContentDAO;
        this.appDocumentDAO = appDocumentDAO;
        this.appPhotoDao = appPhotoDao;
    }

    @Override
    public AppDocument processDoc(Message externalMessage) {
        Document telegramDoc = externalMessage.getDocument();
        String fileId = telegramDoc.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDAO.save(transientAppDoc);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        String filePath = getFilePath(response);
        byte[] fileInByte = downloadFile(filePath);
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .build();
        return binaryContentDAO.save(transientBinaryContent);
    }

    private String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    @Override
    public AppPhoto processPhoto(Message message) {
        //TODO пока что обрабатываем только одно фото в сообщении
        PhotoSize telegramPhoto = message.getPhoto().get(0);
        String fileId = telegramPhoto.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            AppPhoto transientAppPhoto = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return appPhotoDao.save(transientAppPhoto);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    private AppPhoto buildTransientAppPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(telegramPhoto.getFileSize())
                .build();
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token,
                fileId
        );
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }

        //TODO подумать над оптимизацией
        try (InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }


    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }
}
