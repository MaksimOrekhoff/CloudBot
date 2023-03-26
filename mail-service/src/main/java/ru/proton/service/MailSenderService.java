package ru.proton.service;

import ru.proton.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
