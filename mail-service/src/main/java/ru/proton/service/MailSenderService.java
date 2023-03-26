package ru.proton.service;

import ru.proton.utils.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
