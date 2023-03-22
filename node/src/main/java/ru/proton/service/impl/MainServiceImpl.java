package ru.proton.service.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.proton.dao.AppUserDao;
import ru.proton.dao.RawDataDAO;
import ru.proton.entity.AppUser;
import ru.proton.entity.RawData;
import ru.proton.entity.enums.UserState;
import ru.proton.service.MainService;
import ru.proton.service.ProducerService;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final AppUserDao appUserDao;
    private final ProducerService producerService;

    public MainServiceImpl(RawDataDAO rawDataDAO, AppUserDao appUserDao, ProducerService producerService) {
        this.rawDataDAO = rawDataDAO;
        this.appUserDao = appUserDao;
        this.producerService = producerService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        Message textMessage = update.getMessage();
        User telUser = textMessage.getFrom();
        AppUser appUser = findOrSaveAppUser(telUser);

        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("lesson 6");
        producerService.producerAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }

    private AppUser findOrSaveAppUser(User telegramUser) {
        AppUser persistentAppUser = appUserDao.findAppUsersByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(UserState.BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser;
    }
}
