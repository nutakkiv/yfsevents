package com.yfs.application.yfseventsserver.controller;

import com.yfs.application.yfseventsserver.repository.VolunteerRepository;
import com.yfs.application.yfseventsserver.repository.VolunteersAcceptedRepository;
import com.yfs.application.yfseventsserver.entity.Email;
import com.yfs.application.yfseventsserver.entity.Event;
import com.yfs.application.yfseventsserver.entity.Volunteer;
import com.yfs.application.yfseventsserver.entity.VolunteersAccepted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

@Controller
@RequestMapping("/sendEmail")
public class EmailController {

    @Autowired
    Email email;

    @Autowired
    VolunteersAcceptedRepository  volunteersAcceptedRepository;


    public static Properties setProperties()
    {
        String host = "smtp.gmail.com";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        return props;
    }
    public static Session createSession(Properties props, final String username, final String password)
    {
        Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        return session;
    }

    public static boolean sendMail(Session session,String from, String to,String Subject,final String messageText)
    {
        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject(Subject);

            // Now set the actual message
            message.setText(messageText);

            // Send message
            Transport.send(message);

            System.out.println("Sent message successfully....");
            return true;

        } catch (MessagingException e) {

            return false;
        }
    }

    public static Boolean sendMailController(String to,String Subject,String Content)
    {
        System.out.println(to);
        boolean result=false;
        String from = "rainatushar221995221995@gmail.com";
        String username = "rainatushar221995221995@gmail.com";
        String password = "youthforseva";
        String host = "smtp.gmail.com";
        Properties props = setProperties();
        Session session=createSession(props,username,password);

        result=sendMail(session,from,to,Subject,Content);
        System.out.println(result);


        return result;
    }
    public String createUrl(String emailId,String eventId)
    {
        String uniqueUrl="";
        uniqueUrl=uniqueUrl+"http://localhost:8080/"+emailId.split("@")[0]+"/"+emailId.split("@")[1]+"/"+eventId;
        System.out.println("Unique Link is "+uniqueUrl);
        return uniqueUrl;
    }
    public HashMap<String,Boolean> sendMailToall(Email email)
    {
        System.out.println(email.toString());
        System.out.println(email.getEvent().toString());
        HashMap<String,Boolean> result= new HashMap<>();
        System.out.println("Test : "+email.getToMultiple().toString());
        List<String> toMultiple =email.getToMultiple();
        System.out.println(" chk :"+toMultiple.toString());
        toMultiple.parallelStream().flatMap(toUnique-> Stream.of(toUnique)).forEach(toUnique -> result.put(toUnique,sendMailController(
            toUnique,email.getSubject(),email.getText()+" -- "+
                createUrl(toUnique,Long.toString(email.getEvent().getId())))));
        result.forEach((toEmail,response)->insertVolunteerValuesToDB(toEmail,email.getEvent().getId(),response));
        return result;
    }

    public List<String> parseString(String to)
    {
        List<String> toMultiple;
        String[] toMultipleString=to.split(",");
        toMultiple= Arrays.asList(toMultipleString);
        return toMultiple;
    }
    public void insertVolunteerValuesToDB(String email,Long eventId ,Boolean response)
    {
        if(response==false)
            return;
        System.out.println(email+" "+eventId+" "+response);
        VolunteersAccepted volunteersAccepted= new VolunteersAccepted();
        volunteersAccepted.setAccepted(false);
        volunteersAccepted.setMailId(email);
        volunteersAccepted.setEventId(eventId);
        System.out.println("mail is :"+volunteersAccepted.getMailId());
        System.out.println("Try");

        //System.out.println(volunteersAcceptedRepository.getAccepteVolunteersCount(123L));
        VolunteersAccepted v=volunteersAcceptedRepository.save(volunteersAccepted);
    }

    @ResponseBody
    @PostMapping("/send")
            public HashMap<String ,Boolean> sendmail(@RequestBody Email em) {
        System.out.println(em.toString());
        System.out.println(em.getTo());
        System.out.println(em.getSubject());
        em.setToMultiple(parseString(em.getTo()));
        HashMap<String,Boolean>result=sendMailToall(em);
        //Got all the information already
        //System.out.println(event.getEventCategory());
       // return "You have successFully Submitted Data : "+text;
       /* System.out.println(Text);
        String to =Text.split("\"to\":\"")[1];
        to=to.split("\"")[0];
        String subject=Text.split("\"subject\":\"")[1];
        subject=subject.split("\"")[0];
        String text=Text.split("\"text\":\"")[1];
        text=text.split("\"")[0];
        System.out.println(to);
        System.out.println(subject);
        System.out.println(text);
        email.setTo(to);ppp;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
        email.setText(text);
        System.out.println(email.getTo());
        System.out.println(email.getText());
        System.out.println(email.getSubject());
        if(sendMailController(to, subject,text))
        return "{\"status\":\"sentmail\"}";
        else*/
            return result;
    }
//    public static void main(String[] args) {
//        Email email= new Email();
//        email.setTo("rainatushar221995@gmail.com,wwwwww@gmail.com,random1@yahoo.com");
//        email.setSubject("Subject Test3 again");
//        email.setText("Text is working");
//        Event event = new Event();
//        event.setId(312);
//        email.setEvent(event);
//        EmailController emailController=new EmailController();
//        System.out.println(emailController.sendmail(email));
//    }
    public void fun()
    {
        Email email= new Email();
        email.setTo("rainatushar221995@gmail.com,wwwwww@gmail.com,random1@yahoo.com");
        email.setSubject("Subject Test3 again");
        email.setText("Text is working");
        Event event = new Event();
        event.setId(312);
        email.setEvent(event);
        EmailController emailController=new EmailController();
        System.out.println(emailController.sendmail(email));
    }
}
