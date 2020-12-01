import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

public class MailService {

    /**
     * @param credentials  - String array of credentials
     * @param emailAddress - string representation of email address
     * @param emailBody    - string representation of email body
     * @return boolean value if the Gmail was able to connect to the smtp server using the proper email and password
     * Note - password uses authentication for the username
     */

    private boolean sendEmail(Credentials credentials, String emailAddress, String emailBody) {

        String from = credentials.getEmail();
        String host = "smtp.gmail.com";
        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(credentials.getEmail(), credentials.getEmailPassword());
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
     * Performs a validation of the email address used with the following arguments:
     *
     * @param location         - location must be integer type and length of exactly 5
     * @param messageProductID - product Id of type string validating if it exists in the database
     * @param messageQuantity  - product quantity of type integer, makes sure > 0 and integer type
     * @param qm               - creates a new query
     * @return boolean value
     * Note - all previously mentioned arguments must be valid for email to return true
     * @throws SQLException
     */

    private boolean validateEmail(String location, Queue<String> messageProductID, Queue<String> messageQuantity, QueryMaker qm) throws SQLException {

        try {
            Integer.parseInt(location);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        if (location.length() != 5)
            return false;

        while (messageProductID.peek() != null) {
            String productID = messageProductID.poll();
            if (!(qm.valueExists("product_id", "dim_product", productID)))
                return false;
        }

        while (messageQuantity.peek() != null) {
            String quantity = messageQuantity.poll();
            try {
                Integer.parseInt(quantity);
            } catch (NumberFormatException e) {
                return false;
            } catch (NullPointerException e) {
                return false;
            }
            if (Integer.parseInt(quantity) <= 0)
                return false;
        }
        return true;
    }

    /**
     * This method will perform create a proper connection to the Gmail server,
     * validate the proper credentials are there for logging into the email,
     * creates a new query for the email to be read,
     * Once valid login has be acquired the email content can be accessed
     * Regardless of the amount of orders place, product Id and quantity are stored in seperate lists
     * If the message content is empty return message
     * Check if the order email is a cancellation email and handle cancellation process
     * Cancellation process will send email to customer and cancel order for further processing
     * Otherwise send a confirmation email to customer stating order received and is being processed
     * send the valid orders to be processed to our inventory for processing via MySQL after proper formatting
     *
     * @param credentials - pass along proper credentials
     * @param qm          - create a new query
     */

    public void readEmail(Credentials credentials, QueryMaker qm) {
        String host = "pop.gmail.com";
        String user = credentials.getEmail();
        String password = credentials.getEmailPassword();

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

            LinkedList<Transaction> orders = new LinkedList<>();
            LinkedList<String> canceled_Orders = new LinkedList<>();

            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];
                message.getFlags();
                String contentType = message.getContentType();
                String messageContent = "";

                if (contentType.contains("multipart")) {
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        messageContent = part.getContent().toString();
                        break;
                    }
                } else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content1 = message.getContent();

                }

                if (messageContent != null) {
                    messageContent = messageContent;

                    Queue<String> messageProductID = new LinkedList<>();
                    Queue<String> messageQuantity = new LinkedList<>();
                    String[] emailInput = messageContent.split(",");

                    String sender = message.getFrom()[0].toString();
                    sender = sender.substring(sender.indexOf("<") + 1, sender.indexOf(">"));

                    if (emailInput[0].trim().equalsIgnoreCase("cancel")) {
                        System.out.println("Cancellation Email");
                        if (!(canceled_Orders.contains(sender)))
                            canceled_Orders.add(sender);
                        continue;
                    }

                    String location = emailInput[0];

                    for (int k = 1; k < emailInput.length; k = k + 2) {
                        messageProductID.add(emailInput[k].trim());
                        messageQuantity.add((emailInput[k + 1]).trim());
                    }

                    if (validateEmail(location, new LinkedList<>(messageProductID), new LinkedList<>(messageQuantity), qm)) {

                        System.out.println("Valid order from email");

                        String date;

                        date = (message.getSentDate().getYear() + 1900) + "-" + (message.getSentDate().getMonth() + 1) + "-" + message.getSentDate().getDate();

                        String products = messageProductID.toString();

                        while (messageProductID.peek() != null) {
                            orders.add(new Transaction(java.sql.Date.valueOf(date), location, messageProductID.poll(), Integer.parseInt(messageQuantity.poll()), sender));
                        }

                        sendEmail(credentials, message.getFrom()[0].toString(), "Order received. Your order will be stored to be processed\n" + "Your order included these products:\n" + products);
                    } else {
                        System.out.println("Invalid order from email");
                        sendEmail(credentials, message.getFrom()[0].toString(), "Order not received. One of the inputs is invalid\n" + "The products you attempted to order:\n" + messageProductID);
                    }
                }
            }

            Iterator ordersIter = orders.iterator();
            while (ordersIter.hasNext()) {
                Transaction t = (Transaction) ordersIter.next();
                if (canceled_Orders.contains(t.getCustEmail())) {
                    System.out.println("Order removed due to cancellation email");
                    sendEmail(credentials, t.getCustEmail(), "Your order for " + t.getProduct_ID() + " has been canceled");
                    ordersIter.remove();
                }
            }

            if (!(orders.isEmpty())) {
                String[] headers = "date,cust_email,cust_location,product_id,product_quantity".split(",");
                Object[][] objArr = new Object[orders.size()][5];
                for (int l = 0; l < orders.size(); l++) {
                    objArr[l][0] = qm.valueQueryPrep(orders.get(l).getDate());
                    objArr[l][1] = QueryMaker.valueQueryPrep(orders.get(l).getCustEmail());
                    objArr[l][2] = orders.get(l).getCustLocation();
                    objArr[l][3] = QueryMaker.valueQueryPrep(orders.get(l).getProduct_ID());
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
