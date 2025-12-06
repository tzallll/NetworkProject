//αυτόματη εισαγωγή βιβλιοθηκών
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
     //αντιστοίχιση λογαριασμών και token με όνομα και αριθμό με συμμόρφωση στον ταυτοχρονισμό
    private static Map<String, Account> accounts = new ConcurrentHashMap<>();
    private static Map<Integer, String> authTokens = new ConcurrentHashMap<>();
    //ακέραιες πράξεις στα threads,χωρίς sychronized
    private static AtomicInteger nextToken = new AtomicInteger(1000);
    private static AtomicInteger nextMessageId = new AtomicInteger(1);
    //main method of server
    public static void main(String[] args) {
        //εισαγωγή port
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter port number to run server: ");
        int port = Integer.parseInt(sc.nextLine());
        //με το pool έχω πολλαπλά νήματα και επιτρέπεται η επαναχρησιμοποιηση τους
        ExecutorService pool = Executors.newCachedThreadPool();
       //δημιουργία socket με την κατάλληλη θύρα για διαχείριση πελάτη
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.submit(() -> handleClient(clientSocket));
            }
      //εξαίρεση
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        //αποστολή και διάβασμα δεδομένων στο socket
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
         //αν η απαίτηση ενος πελάτη δεν είναι κενή, χειρισμός της
            String request;
            while ((request = in.readLine()) != null) {
                handleRequest(request, out);
            }
         //εξαίρεση
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(String request, PrintWriter out) {
        //χωρισμός της απαίτησης για καλύτερη διαχείριση
        String[] parts = request.split(" ", 3);
        //αν δεν έχω συμβολοσειρά μήνυμα λάθους και τερματισμός
        if (parts.length < 1) {
            out.println("Invalid request");
            out.println("END");
            return;
        }
          //κωδικός για συναρτησεις λειτουργιών χειριστή
        int fnId;
        try {
            //το πρώτο μέρος της συμβολοσειράς ο αριθμός της συνάρτησης και μετατροπή σε ακέραιο
            fnId = Integer.parseInt(parts[0]);
        }//εξαίρεση
        catch (NumberFormatException e) {
            out.println("Invalid function ID");
            out.println("END");
            return;
        }
        //switch-case με κλήση των μεθόδων,αλλιώς μηνύματα λάθους,αν δεν έχω εισαγωγή των απαιτούμενων ορισμάτων της συμβολοσειράς
        switch (fnId) {
            case 1:
                if (parts.length < 2) { out.println("Missing username"); out.println("END"); break; }
                createAccount(parts[1], out);
                break;
            case 2:
                if (parts.length < 2) { out.println("Missing auth token"); out.println("END"); break; }
                showAccounts(parts[1], out);
                break;
            case 3:
                if (parts.length < 3) { out.println("Invalid send message request"); out.println("END"); break; }
                sendMessage(parts[1], parts[2], out);
                break;
            case 4:
                if (parts.length < 2) { out.println("Missing auth token"); out.println("END"); break; }
                showInbox(parts[1], out);
                break;
            case 5:
                if (parts.length < 3) { out.println("Invalid read message request"); out.println("END"); break; }
                readMessage(parts[1], parts[2], out);
                break;
            case 6:
                if (parts.length < 3) { out.println("Invalid delete message request"); out.println("END"); break; }
                deleteMessage(parts[1], parts[2], out);
                break;
                //άγνωστη συνάρτηση αν δεν έχω κλήση καμίας μεθόδου
            default:
                out.println("Unknown function ID");
                out.println("END");
        }
    }
    //1η συνάρτηση:Δημιουργία λογαριασμού.Ορίσματα:όνομα χρήστη και αντικείμενο socket για εκτύπωση
    private static void createAccount(String username, PrintWriter out) {
        //Συνθήκες:όχι ίδια ονόματα και εκτός κανονικών εκφράσεων
        if (!username.matches("[a-zA-Z0-9_]+")) { out.println("Invalid Username"); out.println("END"); return; }
        if (accounts.containsKey(username)) { out.println("Sorry, the user already exists"); out.println("END"); return; }
        //με κάθε εισαγωγή αυξάνω το AtomicInteger του token
        int token = nextToken.getAndIncrement();
        //κλήση λογαριασμού
        Account acc = new Account(username, token);
        //βάζω λογαριασμό και αναγνωριστικό στην λίστα
        accounts.put(username, acc);
        authTokens.put(token, username);
        //εκτύπωση token
        out.println(token);
        //αλλιώς τερματισμός
        out.println("END");
    }
     //2η συνάρτηση:Εμφάνιση λογαριασμών.Αντιστοίχιση κάθε αναγνωριστικού πελάτη με ονόματα
     //από λίστα λογαριασμών
    private static void showAccounts(String tokenStr, PrintWriter out) {
        // παίρνω το όνομα χρήστη στο socket
        String user = getUserFromToken(tokenStr, out);
        if (user == null) return;
       //εκτύπωση σε σειρά
        int i = 1;
        for (String uname : accounts.keySet()) {
            out.println(i + ". " + uname);
            i++;
        }
        //αλλιώς τερματισμός
        out.println("END");
    }
      //3η συνάρτηση:Διάβασμα Μηνύματος, επιπλέον όρισμα  ο πααραλήπτης μηνύματος
    private static void sendMessage(String tokenStr, String rest, PrintWriter out) {
        // παίρνω το όνομα χρήστη στο socket
        String user = getUserFromToken(tokenStr, out);
        if (user == null) return;
          //τοποθετώ σωστά μέρη ορισμάτων(παραλήπτης,μήνυμα)
        String[] subparts = rest.split(" ", 2);
        if (subparts.length < 2) { out.println("Invalid message"); out.println("END"); return; }

        String recipient = subparts[0];
        String body = subparts[1];
        //εξαγωγή παραλήπτη από λίστα λογαριασμών για επιβεβαίωση ύπαρξης
        Account recAcc = accounts.get(recipient);
        //Μήνυμα λάθους
        if (recAcc == null) { out.println("Recipient does not exist"); out.println("END"); return; }
         //Aύξηση κωδικού μηνύματος από AtomicInteger
        Message msg = new Message(nextMessageId.getAndIncrement(), user, recipient, body);
        recAcc.getMessageBox().add(msg);
        //Επιβεβαίωση αποστολής
        out.println("OK");
        //τερματισμός
        out.println("END");
    }
  //4η συνάρτηση:Εμφάνιση Καταλόγου Μηνύματος, επιπλέον όρισμα ο κωδικός μηνύματος
    private static void showInbox(String tokenStr, PrintWriter out) {
        // παίρνω το όνομα χρήστη στο socket(πρέπει να υπάρχει)
        String user = getUserFromToken(tokenStr, out);
        if (user == null) return;
          //Αν το μήνυμα από τον λογαριασμό χρήστη έχει διαβαστεί εκτύπωση κατάλληλου μηνύματος
        Account accUser = accounts.get(user);
        for (Message m : accUser.getMessageBox()) {
            out.println(m.getId() + ". from:" + m.getSender() + (m.isRead() ? "" : "*"));
        }
        //τερματισμός
        out.println("END");
    }
    //5η συνάρτηση:Διάβασμα μηνύματος από παραλήπτη, επιπλέον όρισμα ο κωδικός μηνύματος
    private static void readMessage(String tokenStr, String msgIdStr, PrintWriter out) {
        // παίρνω το όνομα χρήστη στο socket(πρέπει να υπάρχει)
        String user = getUserFromToken(tokenStr, out);
        if (user == null) return;
        //έλεγχος για σωστό κωδικό μηνύματος αλλιώς εξαίρεση
        int msgId;
        try { msgId = Integer.parseInt(msgIdStr); } catch (NumberFormatException e) { out.println("Invalid message ID"); out.println("END"); return; }
         //Κλήση χρήστη
        Account accUser = accounts.get(user);
        //Κλήση μηνύματος από socket
        Optional<Message> mOpt = accUser.getMessageBox().stream().filter(m -> m.getId() == msgId).findFirst();
        //Αν το μήνυμα έχει σταλεί
        if (mOpt.isPresent()) {
            //Εξαγωγή
            Message m = mOpt.get();
            m.setRead(true);
            //εκτύπωση κατάλληλου μηνύματος
            out.println("(" + m.getSender() + ") " + m.getBody());
        }//αλλιώς μήνυμα λάθους
        else out.println("Message ID does not exist");
        //τερματισμός
        out.println("END");
    }
  //6η συνάρτηση:Διαγραφή Μηνύματος, επιπλέον όρισμα ο κωδικός μηνύματος
    private static void deleteMessage(String tokenStr, String msgIdStr, PrintWriter out) {
        // παίρνω το όνομα χρήστη στο socket(πρέπει να υπάρχει)
        String user = getUserFromToken(tokenStr, out);
        if (user == null) return;
        //έλεγχος για σωστό κωδικό μηνύματος αλλιώς εξαίρεση
        int msgId;
        try { msgId = Integer.parseInt(msgIdStr); } catch (NumberFormatException e) { out.println("Invalid message ID"); out.println("END"); return; }
        //Κλήση χρήστη
        Account accUser = accounts.get(user);
        //εκτύπωση κατάλληλου μηνύματος,αν έχει αφαιρεθεί το μήνυμα από έλεγχο
        boolean removed = accUser.getMessageBox().removeIf(m -> m.getId() == msgId);
        out.println(removed ? "OK" : "Message does not exist");
        //τερματισμός
        out.println("END");
    }
//Μετατροπή αναγνωριστικού σε ακέραιο και απευθείας αντιστοίχιση με χρήστη
    private static String getUserFromToken(String tokenStr, PrintWriter out) {
        try {
            int token = Integer.parseInt(tokenStr);
            String user = authTokens.get(token);
            //μήνυμα λάθους
            if (user == null) { out.println("Invalid Auth Token"); out.println("END"); return null; }
            return user;
        }//εξαίρεση
         catch (NumberFormatException e) { out.println("Invalid Auth Token"); out.println("END"); return null; }
    }
}

