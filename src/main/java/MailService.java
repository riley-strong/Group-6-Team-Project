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
    private boolean sendEmail(Credentials credentials, String emailAddress, String emailBody){

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
        if (location.length() != 5)
            return false;

        //Ensures productID is in our database
        while (messageProductID.peek() != null){
            String productID = messageProductID.poll();
            if (!(qm.valueExists("product_id", "dim_product", productID)))
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
            if (Integer.parseInt(quantity) <= 0)
                return false;
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

            // LinkedList of Transaction object, will be a list of individual orders
            LinkedList<Transaction> orders = new LinkedList<>();
            // LinkedList of emails that wish to cancel their pending orders
            LinkedList<String> canceled_Orders = new LinkedList<>();

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

                    // Obtains and reformats sender's email
                    String sender = message.getFrom()[0].toString();
                    sender = sender.substring(sender.indexOf("<") + 1, sender.indexOf(">"));

                    //Cancellation email is caught here
                    if (emailInput[0].trim().equalsIgnoreCase("cancel")){
                        System.out.println("Cancellation Email");
                        if (!(canceled_Orders.contains(sender)))
                            canceled_Orders.add(sender);
                        continue;
                    }

                    // Obtain location from email, should be just very first field
                    String location = emailInput[0];

                    // Email is divided into inputs and stored in its respective queue
                    for (int k = 1; k < emailInput.length; k = k + 2){
                        messageProductID.add(emailInput[k].trim());
                        messageQuantity.add((emailInput[k+1]).trim());
                    }

                    // Validates email's input
                    if (validateEmail(location, new LinkedList<>(messageProductID), new LinkedList<>(messageQuantity), qm)) {

                        System.out.println("Valid order from email");

                        // Obtains date from gmail API and reformats it for mySQL
                        String date;
                        
                        //Deprecated methods are use here because the "new" methods display the day of the week and the month name instead of the corresponding numbers
                        date = message.getSentDate().getYear() + "";
                        date = "20" + date.substring(1) + "-" + message.getSentDate().getMonth() + "-" + message.getSentDate().getDate();

                        // Copy of all of the products order in a single email to be included in the confirmation email
                        String products = messageProductID.toString();

                        // Add validated orders in the email to a list utilizing the transaction class
                        while (messageProductID.peek() != null) {
                            orders.add(new Transaction(java.sql.Date.valueOf(date), location, messageProductID.poll(),  Integer.parseInt(messageQuantity.poll()), sender ));
                        }

                        // Send email stating order received
                        sendEmail(credentials, message.getFrom()[0].toString(),"Order received. Your order will be stored to be processed\n" + "Your order included these products:\n"   + products);
                    }

                    // One of the orders is invalid, email stating this is sent
                    else {
                        System.out.println("Invalid order from email");
                        sendEmail(credentials, message.getFrom()[0].toString(),"Order not received. One of the inputs is invalid\n" + "The products you attempted to order:\n"  + messageProductID);
                    }
                }
            }

            // Now that all of the inbox's email have been read, remove canceled orders, and send the orders to mySQL
            // Iterate through the list of orders, if the email associated with that order is in our list of emails that sent a cancellation order, remove that order
            Iterator ordersIter = orders.iterator();
            while (ordersIter.hasNext()){
                Transaction t = (Transaction) ordersIter.next();
                if (canceled_Orders.contains(t.getCustEmail())) {
                    System.out.println("Order removed due to cancellation email");
                    sendEmail(credentials, t.getCustEmail(), "Your order for " + t.getProduct_ID() + " has been canceled");
                    ordersIter.remove();
                }
            }

            // 2-D object array to insert all of the orders from all of the emails currently in the inbox to mySQL
            if (!(orders.isEmpty())) {
                String[] headers = "date,cust_email,cust_location,product_id,product_quantity".split(",");
                Object[][] objArr = new Object[orders.size()][5];
                for (int l = 0; l < orders.size(); l++) {
                    objArr[l][0] = qm.valueQueryPrep(orders.get(l).getDate());
                    objArr[l][1] = qm.valueQueryPrep(orders.get(l).getCustEmail());
                    objArr[l][2] = orders.get(l).getCustLocation();
                    objArr[l][3] = qm.valueQueryPrep(orders.get(l).getProduct_ID());
                    objArr[l][4] = orders.get(l).getQuantity();
                }
                qm.setTableName("temp_unprocessed_sales");
                qm.insertRows(headers, objArr);
                System.out.println("Orders put to 'temp_unprocessed_sales'");
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
