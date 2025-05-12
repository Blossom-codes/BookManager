package com.balancee.BookManager.service;

import com.balancee.BookManager.dto.EmailDto;

public interface MailService {
    void sendHtmlEmailAlert(EmailDto emailDto);

}
