import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.Properties;

public class MailService {

    private static Inventory inventory;

    public static void main(String[] args) throws MessagingException, IOException {

        inventory = new Inventory();
        inventory.loadInventory();

        // Recipient's email ID needs to be mentioned.
        String to = "firewh18@gmail.com";

        // Sender's email ID needs to be mentioned
        String from = "teamc1447@gmail.com";

        // Assuming you are sending email from through gmails smtp
        String host = "smtp.gmail.com";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication("teamc1447@gmail.com", "Q2020cs3250");

            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Now set the actual message
            message.setText("This is actual message");

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
        String host2 = "pop.gmail.com";// change accordingly
        String mailStoreType = "pop3";
        String username = "teamc1447@gmail.com";// change accordingly
        String password = "Q2020cs3250";// change accordingly

        check(host2, mailStoreType, username, password);


    }

    public static void check(String host, String storeType, String user,
                             String password)
    {
        try {

            //create properties field
            Properties properties = new Properties();

            properties.put("mail.pop3.host", host);
            properties.put("mail.pop3.port", "995");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("pop3s");

            store.connect(host, user, password);

            //create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);


            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);

            // retrieve the messages from the folder in an array and print it
            //Message[] messages = emailFolder.getMessages();

            Message[] messages = emailFolder.search(unseenFlagTerm);
            System.out.println("messages.length---" + messages.length);



            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                message.getFlags();
                //var content = (MimeMultipart)message.getContent();
                //System.out.println(content.getBodyPart(0));
                //System.out.println(content.getBodyPart(1));

                System.out.println("Text: " + message.getContent());

                String contentType = message.getContentType();
                String messageContent="";

                if (contentType.contains("multipart")) {
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        messageContent = part.getContent().toString();
                    }
                }
                else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content1 = message.getContent();
                    if (content1 != null) {
                        messageContent = content1.toString();

                        String[] emailInput = messageContent.split(",");
                        if(emailInput[0].equals("buy")) {
                            inventory.decrementQuantity(emailInput[1], Integer.parseInt(emailInput[2].trim()));
                        }
                        else {

                            inventory.incrementQuantity(emailInput[1], Integer.parseInt(emailInput[2].trim()));

                        }

                        inventory.update();
                    }
                }
                System.out.println(" Message: " + messageContent);



            }

            //close the store and folder objects
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}