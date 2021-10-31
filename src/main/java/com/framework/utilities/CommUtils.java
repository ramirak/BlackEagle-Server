package com.framework.utilities;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import com.framework.security.configurations.ServerDefaults;

@Service
public class CommUtils implements CommInterface{
	@Override
	public void sendEmail(String sendTo, String text, String subject) {
		final String emailSender = ServerDefaults.SERVER_EMAIL;
		final String passwordSender = ServerDefaults.SERVER_EMAIL_PASSWORD;
		Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true"); // TLS
		Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(emailSender, passwordSender);
			}
		});
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ServerDefaults.SERVER_EMAIL));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendTo));
			message.setSubject(subject);
			message.setText(text);
			Transport.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}
	@Override
	public void sendSMS(String sendTo, String text, String subject) {

		
	}

}
