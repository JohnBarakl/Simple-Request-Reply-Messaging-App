import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * <pre>
 * Μέσω αυτής της κλάσης υλοποιείται η επικοινωνία του χρήστη με τον εξυπηρετητή.
 *
 * Το πρόγραμμα θα δέχεται είσοδο από τον χρήστη μέσω των ορισμάτων της main() που δίνονται κατά την κλήση του
 * προγράμματος και αποστέλλεται στον εξυπηρετητή, ενώ ταυτόχρονα θα λαμβάνει δεδομένα από τον εξυπηρετητή και θα τα
 * προβάλλει κατάλληλα στον χρήστη.
 *
 * Η λειτουργία που επιτελεί το πρόγραμμά εξαρτάται από την είσοδο που δέχεται κατά την κλήση του.
 * </pre>
 * @author Ioannis Baraklilis
 */
public class MessagingClient {
    /** Το socket μέσω του οποίου γίνεται επικοινωνία με τον server. */
    private Socket socketToServer;

    /** Το stream μέσω του οποίου ο client δέχεται δεδομένα από τον server. */
    private DataInputStream fromServer;

    /** Το stream μέσω του οποίου ο client στέλνει δεδομένα προς τον server. */
    private DataOutputStream toServer;

    /**
     * Ο προκαθορισμένος κατασκευαστής της MessagingClient που δημιουργεί μία σύνδεση προς τον Server με τα δοθέντα ορίσματα.
     * @param serverIP Η διεύθυνση IP του Server.
     * @param serverPortNumber Η port στην οποία ακούει ο Server.
     */
    public MessagingClient(String serverIP, String serverPortNumber) throws NumberFormatException, UnknownHostException, IllegalArgumentException, IOException  {
        socketToServer = new Socket(serverIP, Integer.parseInt(serverPortNumber));

        fromServer = new DataInputStream(new BufferedInputStream(socketToServer.getInputStream()));
        toServer = new DataOutputStream(new BufferedOutputStream(socketToServer.getOutputStream()));
    }

    /**
     * Τερματίζει τη σύνδεση με τον Server.
     * @throws IOException Αν υπάρχει σφάλμα με το κλείσιμο των streams ή του socket.
     */
    public void endConnection() throws IOException {
        fromServer.close();
        toServer.close();
        socketToServer.close();
    }


//    /** Η διεύθυνση IP του Server, που δίνεται ως όρισμα. */
//    private InetAddress serverIP;
//
//    /** Η port στην οποία ακούει ο Server, που δίνεται ως όρισμα. */
//    private int serverPortNumber;
//

    // System.out.println("The ip address given could not be determined.")

    /**
     * Η μέθοδος εκκίνησης της διεπαφής του χρήστη με το πρόγραμμα.
     * <pre>
     *
     * Με την κλήση της, ενεργοποιείται η επικοινωνία του χρήστη με τον εξυπηρετητή και αναλόγως των ορισμάτων
     * κλήσης της εφαρμογής (άρα και της main) εκτελείται η αντίστοιχη λειτουργία η οποία περιγράφεται στην τεκμηρίωση
     * της κλάσης.
     *    Τα ορίσματα εισόδου είναι της μορφής: ip port_number FN_ID args, όπου
     *      - ip: Η διεύθυνση IP του Server.
     *      - port number: Η port στην οποία ακούει ο Server.
     *      - FN_ID: Το αναγνωριστικό της λειτουργίας που θα εκτελεστεί.
     *      - args: οι παράμετροι της λειτουργίας.
     *
     *      Η εκάστοτε λειτουργία που εκτελείται εξαρτάται απο το FN_ID των ορισμάτων και είναι οι:
     *      - FN_ID = 1: Create Account. Ορίσματα στην εκτέλεση: ip port_number 1 username.
     *          Δημιουργεί ένα account για το user και χρησιμοποιεί το δοσμένο username.
     *          Η συνάρτηση επιστρέφει ένα μοναδικό κωδικό (token) ο οποίος χρησιμοποιείται για να αυθεντικοποιηθεί ο
     *          χρήστης στα επόμενα αιτήματα του.
     *      - FN_ID = 2: Show Accounts. Ορίσματα στην εκτέλεση: ip port_number 2 authToken.
     *          Δείχνει μια λίστα με όλα τα accounts που υπάρχουν στο σύστημα.
     *      - FN_ID = 3: Send Message. Ορίσματα στην εκτέλεση: ip port_number 3 authToken recipient message_body.
     *          Στέλνει μήνυμα (message_body) στο account με username recipient.
     *      - FN_ID = 4: Show Inbox. Ορίσματα στην εκτέλεση: ip port_number 4 authToken.
     *          Εμφανίζει τη λίστα με όλα τα μηνύματα για έναν συγκεκριμένο χρήστη.
     *          Δείχνει μια λίστα με όλα τα μηνύματα που υπάρχουν στο messagebox του χρήστη.
     *      - FN_ID = 5: ReadMessage. Ορίσματα στην εκτέλεση: ip port_number 5 authToken message_id.
     *          Αυτή η λειτουργία εμφανίζει το περιεχόμενο ενός μηνύματος του χρήστη με id message_id.
     *          Έπειτα το μήνυμα μαρκάρεται ως διαβασμένο.
     *          Αν υπάρχει το μήνυμα το πρόγραμμα εκτυπώνει μήνυμα σφάλματος.
     *      - FN_ID = 6: DeleteMessage. Ορίσματα στην εκτέλεση: ip port number 6 authToken message_id.
     *          Αυτή η λειτουργία διαγράφει το μήνυμα με id message_id.
     * </pre>
     * @param args Ορίσματα κλήσης της διεπαφής του χρήστη.
     *             <p>Πρέπει να είναι του τύπου: ip port_number FN_ID args.</p>
     */
    public static void main(String[] args) {
        // Έλεγχος για το αν ο χρήστης έχει δώσει τουλάχιστον 3 ορίσματα οπότε γίνεται η υπόθεση ότι η μορφή και το
        // περιεχόμενο των ορισμάτων είναι ορθό.
        // Αν υπάρχει κάποιο λάθος στη μορφή ή/και το περιεχόμενο τούς, αυτό ελέγχεται στο αντίστοιχο σημείο χρήσης τους.
        if (args.length < 3) {
            System.out.println("Invalid use of arguments.");
            System.out.println("Correct usage: java client <ip> <port number> <FN_ID> <args>");
        }

        // Αρχικοποίηση σε null ώστε να μπορεί να γίνει διαχείριση κλεισίματος του ακόμα και στην περίπτωση αποτυχίας
        // ανοίγματος.
        MessagingClient client = null;
        try {
            // Δημιουργώ σύνδεση με τον server σύμφωνα με τα αντίστοιχα ορίσματα.
            client = new MessagingClient(args[0], args[1]);

            // Επιλογή λειτουργίας βάσει του <FN_ID> ορίσματος και κλήση αντίστοιχης μεθόδου
            // για υλοποίηση της.
            switch (args[2]) {
                case "1":
                    client.createAccount(args);
                    break;
                case "2":
                    client.showAccounts(args);
                    break;
                case "3":
                    client.sendMessage(args);
                    break;
                case "4":
                    client.showInbox(args);
                    break;
                case "5":
                    client.readMessage(args);
                    break;
                case "6":
                    client.deleteMessage(args);
                    break;
                default: // Αν δοθεί <FN_ID> που δεν αντιστοιχεί σε λειτουργία τυπώνεται μήνυμα λάθους.
                    System.out.println("Invalid use of argument <FN_ID>, no function could be mapped to given argument.");
                    System.out.println("Correct usage: java client <ip> <port number> <FN_ID> <args>");
            }
        } catch (NumberFormatException e) {
            System.out.println("Argument <port_number> is not a valid integer");
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
        } finally {
            // Τερματισμός της σύνδεσης με τον server.
            try {
                if (client != null){ // Απόπειρα κλεισίματος μόνο αν έχει ανοιχθεί επιτυχώς.
                    client.endConnection();
                }
            } catch (IOException e) {
                System.out.println("IO error while closing streams and socket: " + e.getMessage());
            }
        }
    }

    /**
     *  Υλοποίηση της λειτουργίας με FN_ID = 1: Create Account.
     * @param args Τα ορίσματα που δόθηκαν κατά την κλήση του προγράμματος. <br>
     *             Γίνεται έλεγχος εγκυρότητας των ορισμάτων και αν διαπιστωθεί σφάλμα
     *              τυπώνεται αντίστοιχο μήνυμα σφάλματος και γίνεται έξοδος από τη συνάρτηση. <br>
     *              Τυχόντα ορίσματα που ακολουθούν το όρισμα username αγνοούνται.
     */
    private void createAccount(String[] args){
        // Έλεγχος για το αν πληρείται η προϋπόθεση του ότι το όνομα χρήστη αποτελείται μόνο από αλφαριθμητικά και τον
        // ειδικό χαρακτήρα "_".
        if (!args[3].matches("[a-zA-Z0-9_]+")){
            System.out.println("Invalid Username");
            return;
        }

        try {
            // Αποστολή κωδικού λειτουργίας που θέλουμε να εκτελεστεί.
            // Η μορφή του κωδικού λειτουργίας που θα σταλεί στον server
            // θα είναι "$κωδικός_λειτουργίας".
            toServer.writeUTF("$1");
            // Αποστέλλω το username του χρήστη.
            toServer.writeUTF(args[3]);
            // "Καθαρίζω" το buffer ώστε να είναι σίγουρη η αποστολή των δεδομένων.
            toServer.flush();

            // Δέχομαι απάντηση από τον server
            String serverResponse = fromServer.readUTF();

            // Τυπώνω την απάντηση στην έξοδο.
            System.out.println(serverResponse);

        } catch (IOException e) {
            System.out.println("IO Error while communicating with server: " + e.getMessage());
        }
    }

    private void showAccounts(String[] args){

    }

    private void sendMessage(String[] args){

    }

    private void showInbox(String[] args){

    }

    private void readMessage(String[] args){

    }

    private void deleteMessage(String[] args){

    }
}
