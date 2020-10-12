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
        readEmail();
    }

    public static boolean sendConfirmation(String emailAdress, String emailBody ){

        String from = "teamc1447@gmail.com";
        String host = "smtp.gmail.com";
        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication("teamc1447@gmail.com", "Q2020cs3250");
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAdress));
            message.setSubject("Confirmation");
            message.setText(emailBody);
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            return false;
        }

        return true;
    }

    public static void readEmail()
    {
        String host = "pop.gmail.com";// change accordingly
        String user = "teamc1447@gmail.com";// change accordingly
        String password = "Q2020cs3250";// change accordingly

        try {

            Properties properties = new Properties();
            properties.put("mail.pop3.host", host);
            properties.put("mail.pop3.port", "995");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            Store store = emailSession.getStore("pop3s");
            store.connect(host, user, password);

            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);

            Message[] messages = emailFolder.search(unseenFlagTerm);
            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];
                message.getFlags();
                String contentType = message.getContentType();
                String messageContent="";

                if (contentType.contains("multipart")) {
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        messageContent = part.getContent().toString();
                        break;
                    }
                }
                else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content1 = message.getContent();

                }

                if (messageContent != null) {
                    messageContent = messageContent.toString();

                    String[] emailInput = messageContent.split(",");
                    if(emailInput[0].equals("buy")) {
                       inventory.decrementQuantity(emailInput[1], Integer.parseInt(emailInput[2].trim()));
                       inventory.update();
                        sendConfirmation(message.getFrom()[0].toString(),"Thank you for buying: "  + emailInput[1]);
                    }
                    else {
                        inventory.incrementQuantity(emailInput[1], Integer.parseInt(emailInput[2].trim()));
                        inventory.update();
                        sendConfirmation(message.getFrom()[0].toString(),"Thank you for selling: "  + emailInput[1]);
                    }
                }
            }
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