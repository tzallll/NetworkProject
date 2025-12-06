//βιβλιοθήκες
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable {
    //έλεγχος matching αντικειμένου με την τρέχουσα έκδοση κλάσης(το εμφάνισε αυτόματα το Intellij)
    private static final long serialVersionUID = 1L;
    //ιδιότητες
    private String username;
    private int authToken;
    private List<Message> messageBox;
    //κατασκευαστής
    public Account(String username, int authToken) {
        this.username = username;
        this.authToken = authToken;
        this.messageBox = new ArrayList<>();
    }
    //getters
    public String getUsername() { return username; }
    public int getAuthToken() { return authToken; }
    public List<Message> getMessageBox() { return messageBox; }
    //μέθοδος προσθήκης μηνυμάτων στη λίστα
    public void addMessage(Message msg) {
        messageBox.add(msg);
    }
}
