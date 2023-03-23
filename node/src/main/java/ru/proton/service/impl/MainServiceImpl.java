package ru.proton.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.proton.dao.AppUserDao;
import ru.proton.dao.RawDataDAO;
import ru.proton.entity.AppUser;
import ru.proton.entity.RawData;
import ru.proton.entity.enums.UserState;
import ru.proton.service.MainService;
import ru.proton.service.ProducerService;

import static ru.proton.entity.enums.UserState.BASIC_STATE;
import static ru.proton.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static ru.proton.service.enums.ServiceCommands.*;

@Service
@Log4j
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
        AppUser appUser = findOrSaveAppUser(update);
        UserState state = appUser.getState();
        String text = update.getMessage().getText();
        String output = "";

        if (CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(state)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(state)) {
            //TODO добавить обработку почты
        } else {
            log.debug("Unknown user state: " + state);
            output = "Неизвестная команда. Введите /cancel и попробуйте снова!";

        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);


    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранения документа :)
        String answer = "Документ успешно загружен! Ссылка для скачивания: http://test.ru/get-doc/7";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранения фото :)
        String answer = "Фото успешно загружено! Ссылка для скачивания: http://test.ru/get-photo/777";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getState();
        if (!appUser.getIsActive()) {
            String error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента.";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            String error = "Отмените текущую команду с помощью /cancel для отправки файлов.";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String text) {
        if (REGISTRATION.equals(text)) {
            //TODO Add registration
            return "Временно недоступно";
        } else if (HELP.equals(text)) {
            return help();
        } else if (START.equals(text)) {
            return "Приветствую! Введи /help для просмотра возможносей!";
        } else {
            return "Неизвестная команда! Введи /help для просмотра возможносей!";
        }
    }

    private String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя.";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Команда отменена!";
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();

        AppUser persistentAppUser = appUserDao.findAppUsersByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser;
    }
}
