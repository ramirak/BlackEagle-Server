package com.framework.communication;

public interface CommInterface {
	public void sendEmail(String sendTo, String text, String subject);
	
	public void sendSMS(String sendTo, String text, String subject);
}
