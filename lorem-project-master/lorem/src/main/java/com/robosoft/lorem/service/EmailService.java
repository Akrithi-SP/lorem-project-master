package com.robosoft.lorem.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService
{
    private static final String username="akrithispoojary@gmail.com";
    private static final String password="cixivrhzupcocqtx";
    public boolean sendEmail(String email, int tfaCode) throws MessagingException {
        Properties props= new Properties();
        props.put("mail.smtp.auth","true");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.host","smtp.gmail.com");
        props.put("mail.smtp.port","587");

        Session session= Session.getInstance(props, new javax.mail.Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication(){
                return  new PasswordAuthentication(username,password);
            }
        });

        MimeMessage message= new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.addRecipient(Message.RecipientType.TO,new InternetAddress(email));

        message.setSubject("Two Factor Authentication code from our Service");
        message.setText("Your Two Factor Authentication code is:"+tfaCode);
        Transport.send(message);
        return true;
    }
}