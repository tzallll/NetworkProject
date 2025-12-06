//Αυτόματη εισαγωγή βιβλιοθηκών
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
  //main of client
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String authToken = null;
       //Eισαγωγή ΙP
        System.out.print("Enter server IP: ");
        String ip = sc.nextLine();
       //Εισαγωγή θύρας
        System.out.print("Enter server port: ");
        int port = Integer.parseInt(sc.nextLine());
           //socket client with suitable port(διάβασμα και εγγραφή)
        try (Socket socket = new Socket(ip, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             //flush χρησιμοποιείται για να μην παρεμποδίζεται το buffer
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            while (true) {
                //Mενού διαχείρισης
                System.out.println("\n--- Menu ---");
                System.out.println("1. Create Account");
                System.out.println("2. Show Accounts");
                System.out.println("3. Send Message");
                System.out.println("4. Show Inbox");
                System.out.println("5. Read Message");
                System.out.println("6. Delete Message");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");
                //έλεγχος για σωστή κλήση λειτουργίας
                int fnId;
                try {
                    fnId = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                    continue;
                }

                if (fnId == 0) break;
               //Μάζεμα όλης την συμβολοσειράς και τοποθέτηση στην κατάλληλη περίπτωση
                String request = fnId + " ";

                switch (fnId) {
                    case 1:
                        //εισαγωγή χρήστη
                        System.out.print("Enter username: ");
                        request += sc.nextLine();
                        break;
                    case 2: case 4:
                        //εισαγωγή αναγνωριστικού(ισχύει και στην 2 και 4 περίπτωση)
                            System.out.print("Enter authToken: ");
                            authToken = sc.nextLine();
                        request += authToken;
                        break;
                    case 3:
                        //εισαγωγή token παραλήπτη
                        if (authToken == null) {
                            System.out.print("Enter authToken: ");
                            authToken = sc.nextLine();
                        }
                        //εισαγωγή παραλήπτη
                        request += authToken + " ";
                        System.out.print("Enter recipient: ");
                        request += sc.nextLine() + " ";
                        //εισαγωγή μηνύματος
                        System.out.print("Enter message: ");
                        request += sc.nextLine();
                        break;
                        // είσοδος token παραλήπτη και κωδικού μηνύματος και στις 2 περιπτώσεις
                    case 5: case 6:
                        if (authToken == null) {
                            System.out.print("Enter authToken: ");
                            authToken = sc.nextLine();
                        }
                        request += authToken + " ";
                        System.out.print("Enter message ID: ");
                        request += sc.nextLine();
                        break;
                        //ένδειξη για λάθος επιλογή
                    default:
                        System.out.println("Invalid option");
                        continue;
                }
                  //εκτύπωσε απαίτηση
                out.println(request);
                //εκτύπωσε οτιδήποτε από τις λειτουργίες,εφόσον είχε υπάρξει εισαγωγή στο socket
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("END")) break;
                    System.out.println(line);
                }
            }//εξαίρεση
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

