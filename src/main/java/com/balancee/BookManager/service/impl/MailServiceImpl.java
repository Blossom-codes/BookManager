package com.balancee.BookManager.service.impl;

import com.balancee.BookManager.dto.EmailDto;
import com.balancee.BookManager.service.MailService;
import com.balancee.BookManager.utils.LoggingUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendHtmlEmailAlert(EmailDto emailDto) {
        LoggingUtils.DebugInfo("About to send onboarding mail >>>>>>> "+emailDto.getRecipient());

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(emailDto.getRecipient());
            helper.setSubject(emailDto.getSubject());
            helper.setText(emailDto.getMessage(), true);  // `true` enables HTML
            LoggingUtils.DebugInfo("Onboarding mail : "+emailDto.getMessage());

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LoggingUtils.DebugInfo("An error has occurred while sending onboarding mail "+e);
            e.printStackTrace();
        }
    }
}
