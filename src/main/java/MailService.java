import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.sql.SQLException;
import java.util.*;

public class MailService {

    private static QueryMaker qm;

    static {
        try {
            qm = Credentials.databaseLogin();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean sendConfirmation(String emailAdress, String emailBody ){

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
            message.setSubject("Your Order");
            message.setText(emailBody);
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            return false;
        }

        return true;
    }

    private static boolean validateEmail(Queue<String> messageOperation, Queue<String> messageProductID, Queue<String> messageQuantity) throws SQLException {
        qm.setTableName("inventory");
        while (messageOperation.peek() != null){
            String operation = messageOperation.poll();
            if (!(operation.equals("buy") || operation.equals("sell")))
                return false;
        }
        while (messageProductID.peek() != null){
            String productID = messageProductID.poll();
            if (!(qm.contains(productID)))
                return false;
        }
        while (messageQuantity.peek() != null){
            String quantity = messageQuantity.poll();
            try {
                Integer.parseInt(quantity);
            } catch(NumberFormatException e) {
                return false;
            } catch(NullPointerException e) {
                return false;
            }
        }
        return true;
    }

    public static void readEmail() {
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
                    Queue<String> messageOperation = new LinkedList<>();
                    Queue<String> messageProductID = new LinkedList<>();
                    Queue<String> messageQuantity = new LinkedList<>();
                    String[] emailInput = messageContent.split(",");
                    if (emailInput[0].equalsIgnoreCase("cancel")){ // cancel email requires it to state cancel,0,0 or we get an array out of bounds error, not sure how else to fix this
                        System.out.println("place holder for an email to cancel");
                        return;
                    }
                    for (int k = 0; k < emailInput.length; k = k + 3){
                        messageOperation.add(emailInput[k]);
                        messageProductID.add(emailInput[k+1]);
                        messageQuantity.add((emailInput[k+2]).trim());
                    }

                    if (validateEmail(new LinkedList<>(messageOperation), new LinkedList<>(messageProductID), new LinkedList<>(messageQuantity))) {
                        System.out.println("Valid");
                        System.out.println(messageOperation);
                        System.out.println(messageProductID);
                        System.out.println(messageQuantity);
                        sendConfirmation(message.getFrom()[0].toString(),"Order received. Your order will be stored to be processed\n" + "Your order included these products\n"   + messageProductID);
                    }
                    else {
                        System.out.println("Invalid");
                        System.out.println(messageOperation);
                        System.out.println(messageProductID);
                        System.out.println(messageQuantity);
                        sendConfirmation(message.getFrom()[0].toString(),"Order not received. One of the inputs is invalid\n" + "The products you attempted to order:\n"  + messageProductID);
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

    public static void main(String[] args) {
        readEmail();
    }


}