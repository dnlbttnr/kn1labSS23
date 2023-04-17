import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage; 

public class Send_Mail {
	public static void main(String[] args) {
		sendMail();   
	}
	
	public static void sendMail() {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.host", "localhost");
			Session session = Session.getInstance(props);

			try {
				MimeMessage msg = new MimeMessage(session);
				msg.setFrom("labrat@localhost");
				msg.setRecipients(Message.RecipientType.TO,
						"labrat@localhost");
				msg.setSubject("JavaMail Test Mail");
				msg.setSentDate(new Date());
				msg.setText("Hello, world!\n");
				Transport.send(msg);
			} catch (MessagingException mex) {
				System.out.println("send failed, exception: " + mex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
