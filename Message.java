//βιβλιοθήκη
import java.io.Serializable;

public class Message implements Serializable {
    //έλεγχος matching αντικειμένου με την τρέχουσα έκδοση κλάσης(το εμφάνισε αυτόματα το Intellij)
    private static final long serialVersionUID = 1L;

   //ιδιότητες
    private int id;
    private boolean isRead;
    private String sender;
    private String receiver;
    private String body;
   //κατασκευαστής
    public Message(int id, String sender, String receiver, String body) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
        this.isRead = false;
    }

    // getters
    public int getId() { return id; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getBody() { return body; }
}

