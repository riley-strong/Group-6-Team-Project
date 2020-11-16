import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.sql.SQLException;
import java.util.*;

/*
    MailService class is used to send and receive customer orders
 */
public class MailService {

    /**
     * Sends an confirmation email that states whether the order was valid or invalid
     *
     * @param emailAddress  String
     * @param emailBody    String
     * @return a value of the primitive type boolean
     */
    private boolean sendConfirmation(Credentials credentials,String emailAddress, String emailBody){

        String from = credentials.getEmail();
        String host = "smtp.gmail.com";
        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            /**
             * Authenticates password for the username
             */
            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication( credentials.getEmail(), credentials.getEmailPassword());
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
            message.setSubject("Your Order");
            message.setText(emailBody);
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     *
     * @param messageProductID Queue<String>
     * @param messageQuantity Queue<String>
     * @return boolean validateEmail
     * @throws SQLException
     */
    private boolean validateEmail(String location,Queue<String> messageProductID, Queue<String> messageQuantity, QueryMaker qm) throws SQLException {
        
        //Ensures location is valid (5 digits and an int)
        try {
            Integer.parseInt(location);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        if (Integer.parseInt(location) < 1000)
            return false;

        //Ensures productID is in our database
        while (messageProductID.peek() != null){
            String productID = messageProductID.poll();
            if (!(qm.valueExists("product_id", "inventory", productID)))
                return false;
        }

        // Ensures quantity is an integer
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

    /**
     *  Reads the content of the e-mail
     */
    public void readEmail(Credentials credentials, QueryMaker qm) {
        String host = "pop.gmail.com";// change accordingly
        String user = credentials.getEmail();// change accordingly
        String password = credentials.getEmailPassword();// change accordingly

        try {
            //assigning properties for the session
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
            //searching for the unseen messages in the e-mail folder
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

                //read message content if not null
                if (messageContent != null) {
                    messageContent = messageContent.toString();

                    // 3 Queue's to store the individual each input of an order
                    Queue<String> messageProductID = new LinkedList<>();    // Stores order's product ID
                    Queue<String> messageQuantity = new LinkedList<>(); // Stores order's quantity
                    String[] emailInput = messageContent.split(","); //Splits CSV formatted email into text fields

                    //Cancellation email is caught here
                    // TODO: 11/9/20 Implement handling cancellation orders by removing the respective orders from unprocessed_sales
                    if (emailInput[0].trim().equalsIgnoreCase("cancel")){
                        System.out.println("place holder for an email to cancel");
                        return;
                    }

                    // Obtain location from email, should be just very first field
                    String location = emailInput[0];

                    // Email is divided into inputs and stored in its respective queue
                    for (int k = 1; k < emailInput.length; k = k + 2){
                        messageProductID.add(emailInput[k]);
                        messageQuantity.add((emailInput[k+1]).trim());
                    }

                    // Validates email's input
                    if (validateEmail(location, new LinkedList<>(messageProductID), new LinkedList<>(messageQuantity), qm)) {

                        // Obtains date from gmail API and reformats it for mySQL
                        System.out.println("Valid order from email");
                        String date;
                        
                        //Deprecated methods are use here because the "new" methods display the day of the week and the month name instead of the corresponding numbers
                        date = message.getSentDate().getYear() + "";
                        date = "20" + date.substring(1) + "-" + message.getSentDate().getMonth() + "-" + message.getSentDate().getDate();

                        // Obtains and reformats sender's email
                        String sender = message.getFrom()[0].toString();
                        sender = sender.substring(sender.indexOf("<") + 1, sender.indexOf(">"));

                        // Copy of all of the products order in a single email to be included in the confirmation email
                        String products = messageProductID.toString();

                        // 2-D object array to insert all of the orders in a single email to mySQL
                        String[] headers = "date,cust_email,cust_location,product_id,product_quantity".split(",");
                        Object[][] objArr = new Object[messageProductID.size()][5];
                        for (int l = 0; messageProductID.peek() != null; l ++) {
                            objArr[l][0] = qm.valueQueryPrep(date);
                            objArr[l][1] = qm.valueQueryPrep(sender);
                            objArr[l][2] = location;
                            objArr[l][3] = qm.valueQueryPrep(messageProductID.poll());
                            objArr[l][4] = messageQuantity.poll();
                        }
                        qm.setTableName("temp_unprocessed_sales");
                        qm.insertRows(headers, objArr);

                        // Send email stating order received
                        sendConfirmation(credentials, message.getFrom()[0].toString(),"Order received. Your order will be stored to be processed\n" + "Your order included these products:\n"   + products);
                    }
                    // One of the orders is invalid, email stating this is sent
                    else {
                        System.out.println("Invalid order from email");
                        sendConfirmation(credentials, message.getFrom()[0].toString(),"Order not received. One of the inputs is invalid\n" + "The products you attempted to order:\n"  + messageProductID);
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