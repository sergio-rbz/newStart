package rbz.portal;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailerView extends Thread {

    private Thread t;
    private String threadName;
    private String email;
    private String uidNumber;
    private String link;
    public ini i = new ini();
    private String emailText = null;

    MailerView(String emailText) {
        this.emailText = emailText;
    }

    MailerView() {
        //System.out.println("Creating " + threadName);
    }

    @Override
    public void run() {
        //System.out.println("Running " + threadName);
        SendMail(email, uidNumber, link);
        //System.out.println("Thread " + threadName + " Mail versendet!.");
    }

    @Override
    public void start() {
        //System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }


    public boolean SendMail(String email, String uidNumber, String link) {
        final String username = i.getMailAddress();
        final String password = i.getMailPassword();
        Properties prop = new Properties();
        prop.put("mail.smtp.host", i.getSmtpHost());
        prop.put("mail.smtp.port", i.getSmtpPort());
        prop.put("mail.smtp.auth", i.getSmtpAuth());
        prop.put("mail.smtp.ssl.enable", i.getSmtpSSL()); //TLS


        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(i.getFromEMail()));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(email)
            );
            message.setSubject(i.getMailSubject(), "UTF-8");
            message.setText(i.getMailWelcome() + "\n" + emailText + "\n" + email + "\ndiesen Link\n" + link, "UTF-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUidNumber() {
        return uidNumber;
    }

    public void setUidNumber(String uidNumber) {
        this.uidNumber = uidNumber;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

}
