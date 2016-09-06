package util
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

 class EmailSender {
	
	public static void sendEmail(String text){
		final String username = "conflict.predictor@gmail.com";
		final String password = "conflict1407";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("conflict.predictor@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse("conflict.predictor@gmail.com"));
			message.setSubject("Conflict Predictor results");
			message.setText(text);

			Transport.send(message);

			System.out.println("Email sent to conflict.predictor@gmail.com");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args){
		EmailSender.sendEmail("teste")
	}
	
}
