import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

public class Receive_Mail {
	public static void main(String[] args) throws Exception {
		fetchMail();
	}
	
	public static void fetchMail() {
		try {
			Properties props = System.getProperties();
			props.put("mail.pop3.host", "localhost");
			Session session = Session.getInstance(props);

			Store store = session.getStore("pop3");
			store.connect("localhost", "labrat", "kn1lab");

			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_ONLY);

			Message[] messages = folder.getMessages();

			for (int i = 0; i < messages.length; i++) {
				System.out.println(messages[i].getFrom()[0]);
				System.out.println(messages[i].getSentDate());
				System.out.println(messages[i].getSubject());
				System.out.println(messages[i].getMessageNumber());
				System.out.println(messages[i].getContent().toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
