package ru.proton.service.impl;

import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.proton.dao.AppDocumentDao;
import ru.proton.dao.BinaryContentDao;
import ru.proton.entity.AppDocument;
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

    public FileServiceImpl(BinaryContentDao binaryContentDAO, AppDocumentDao appDocumentDAO) {
        this.binaryContentDAO = binaryContentDAO;
        this.appDocumentDAO = appDocumentDAO;
    }

    @Override
    public AppDocument processDoc(Message externalMessage) {
        String fileId = externalMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            String filePath = String.valueOf(jsonObject
                    .getJSONObject("result")
                    .getString("file_path"));
            byte[] fileInByte = downloadFile(filePath);
            BinaryContent transientBinaryContent = BinaryContent.builder()
                    .fileAsArrayOfBytes(fileInByte)
                    .build();
            BinaryContent persistentBinaryContent = binaryContentDAO.save(transientBinaryContent);
            Document telegramDoc = externalMessage.getDocument();
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDAO.save(transientAppDoc);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
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
