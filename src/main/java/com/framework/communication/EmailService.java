package com.framework.communication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import com.framework.constants.ServerDefaults;

@Service
public class EmailService {
	private String emailPass;

	public EmailService() {
		try {
			this.emailPass = Files.readString(Paths.get("sec/blackeagle-mail.cred"), StandardCharsets.US_ASCII);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendEmail(String sendTo, String text, String subject) {
		if (!ServerDefaults.SEND_VIA_EMAIL) {
			System.out.println("\n-------------------------------------------------------------------------\n");
			System.out.println("Send To: " + sendTo + "\nSubject: " + subject + "\nMessage: " + text);
			System.out.println("\n-------------------------------------------------------------------------\n");
			return;
		}
		final String emailSender = ServerDefaults.SERVER_EMAIL;
		final String passwordSender = emailPass;
		Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.office365.com");
		prop.put("mail.smtp.port", "587");
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.starttls.required", "true");
		prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

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
}
