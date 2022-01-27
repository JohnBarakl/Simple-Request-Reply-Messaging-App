package client;

import common.ClientQueries;
import common.InvalidAuthTokenException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
    /** Περιέχει το κείμενο που περιγράφει τον ορθό τρόπο κλήσης αυτού του προγράμματος */
    private final static String CORRECT_USAGE = "Correct usage: java client <ip> <port number> <FN_ID> <args>";

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
     *      - FN_ID = 1: Create server.Account. Ορίσματα στην εκτέλεση: ip port_number 1 username.
     *          Δημιουργεί ένα account για το user και χρησιμοποιεί το δοσμένο username.
     *          Η συνάρτηση επιστρέφει ένα μοναδικό κωδικό (token) ο οποίος χρησιμοποιείται για να αυθεντικοποιηθεί ο
     *          χρήστης στα επόμενα αιτήματα του.
     *      - FN_ID = 2: Show Accounts. Ορίσματα στην εκτέλεση: ip port_number 2 authToken.
     *          Δείχνει μια λίστα με όλα τα accounts που υπάρχουν στο σύστημα.
     *      - FN_ID = 3: Send server.Message. Ορίσματα στην εκτέλεση: ip port_number 3 authToken recipient message_body.
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
            System.out.println(CORRECT_USAGE);
            System.exit(1);
        }

        // Λαμβάνω τη διεύθυνση ip σε μεταβλητή.
        String host = args[0];

        // Λαμβάνω τον αριθμό port σε μεταβλητή με παράλληλο έλεγχο ορθότητας δεδομένων.
        int portNumber = stringToIntWithErrorHandling(args[1], String.format("%s%n%s", "Invalid port argument.", CORRECT_USAGE));

        // Εκκίνηση επικοινωνίας με Server.
        try {
            // Σύνδεση με RMI Registry του Server.
            Registry rmiRegistry = LocateRegistry.getRegistry(host, portNumber);

            // Λήψη stub του ClientQueries για αιτήματα προς τον Server.
            ClientQueries queriesToServer = (ClientQueries) rmiRegistry.lookup("client_query_point");

            // Λαμβάνω το FN_ID από τα ορίσματα και επιλέγω και καλώ αντίστοιχη συνάρτηση του stub.
            int functionId = stringToIntWithErrorHandling(args[2],
                    String.format("%s%n%s", "Invalid function id argument.", CORRECT_USAGE));

            switch (functionId){
                case 1:
                    // Έλεγχος για το αν υπάρχει σωστός αριθμός ορισμάτων.
                    if (args.length < 4){
                        System.out.println("Invalid argument number.");
                        System.out.println("Correct usage of create account function: java client <ip> <port number> 1 <username>");
                        System.exit(1);
                    }

                    // Λαμβάνω το username σε μεταβλητή και δημιουργώ λογαριασμό με αυτό και τυπώνω το επιστρεφόμενο
                    // token ή μήνυμα λάθους.
                    String username = args[3];
                    System.out.println(queriesToServer.createAccount(username));
                    break;
                case 2:
                    String case2CorrectUsage = "Correct show accounts function: java client <ip> <port number> 2 <authToken>";
                    // Έλεγχος για το αν υπάρχει σωστός αριθμός ορισμάτων.
                    if (args.length < 4){
                        System.out.println("Invalid argument number.");
                        System.out.println(case2CorrectUsage);
                        System.exit(1);
                    }

                    // Λαμβάνω το authToken μετά από έλεγχο εγκυρότητας του ορίσματος που το περιέχει.
                    int authToken = stringToIntWithErrorHandling(args[3],
                            String.format("%s%n%s", "Invalid authToken argument.", case2CorrectUsage));


                    try{
                        // Τυπώνω τη λίστα χρηστών.
                        String[] accounts = queriesToServer.showAccounts(authToken);
                        for (int i = 0; i < accounts.length; i++) {
                            System.out.printf("%d. %s%n", i, accounts[i]);
                        }
                    } catch (InvalidAuthTokenException e){ // Χειρισμός άκυρου authToken.
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    String case3CorrectUsage = "Correct usage of send message function: " +
                            "java client <ip> <port number> 3 <authToken> <recipient> <message_body>";
                    // Έλεγχος για το αν υπάρχει σωστός αριθμός ορισμάτων.
                    if (args.length < 6){
                        System.out.println("Invalid argument number.");
                        System.out.println(case3CorrectUsage);
                        System.exit(1);
                    }

                    // Λαμβάνω το authToken μετά από έλεγχο εγκυρότητας του ορίσματος που το περιέχει.
                    authToken = stringToIntWithErrorHandling(args[3],
                            String.format("%s%n%s", "Invalid authToken argument.", case3CorrectUsage));

                    // Λαμβάνω το username του παραλήπτη.
                    String recipient = args[4];

                    // Λαμβάνω το περιεχόμενο του μηνύματος
                    String messageBody = args[5];

                    try {
                        // Εμφανίζω στον χρήστη την απάντηση του Server.
                        System.out.println(queriesToServer.sendMessage(authToken, recipient, messageBody));
                    } catch (InvalidAuthTokenException e){ // Χειρισμός άκυρου authToken.
                        System.out.println(e.getMessage());
                    }
                    break;
                case 4:
                    String case4CorrectUsage = "Correct show inbox function: java client <ip> <port number> 4 <authToken>";
                    // Έλεγχος για το αν υπάρχει σωστός αριθμός ορισμάτων.
                    if (args.length < 4){
                        System.out.println("Invalid username argument.");
                        System.out.println(case4CorrectUsage);
                        System.exit(1);
                    }

                    // Λαμβάνω το authToken μετά από έλεγχο εγκυρότητας του ορίσματος που το περιέχει.
                    authToken = stringToIntWithErrorHandling(args[3],
                            String.format("%s%n%s", "Invalid authToken argument.", case4CorrectUsage));

                    try {
                        // Λαμβάνω λίστα με το μηνύματα για τον χρήστη.
                        String[] inbox = queriesToServer.showInbox(authToken);

                        // Τυπώνω το περιεχόμενο της λίστας
                        for (String s : inbox) {
                            System.out.println(s);
                        }
                    } catch (InvalidAuthTokenException e){ // Χειρισμός άκυρου authToken.
                        System.out.println(e.getMessage());
                    }
                    break;
                case 5:
                    String case5CorrectUsage = "Correct usage of read message function: " +
                            "java client <ip> <port number> 5 <authToken> <message_id>";
                    // Έλεγχος για το αν υπάρχει σωστός αριθμός ορισμάτων.
                    if (args.length < 5){
                        System.out.println("Invalid argument number.");
                        System.out.println(case5CorrectUsage);
                        System.exit(1);
                    }

                    // Λαμβάνω το authToken μετά από έλεγχο εγκυρότητας του ορίσματος που το περιέχει.
                    authToken = stringToIntWithErrorHandling(args[3],
                            String.format("%s%n%s", "Invalid authToken argument.", case5CorrectUsage));

                    // Λαμβάνω το username του παραλήπτη.
                    int messageId = stringToIntWithErrorHandling(args[4],
                            String.format("%s%n%s", "Invalid message id argument.", case5CorrectUsage));


                    try {
                        // Εμφανίζω στον χρήστη την απάντηση του Server.
                        System.out.println(queriesToServer.readMessage(authToken, messageId));
                    } catch (InvalidAuthTokenException e){ // Χειρισμός άκυρου authToken.
                        System.out.println(e.getMessage());
                    }
                    break;
                case 6:
                    String case6CorrectUsage = "Correct usage of delete message function: " +
                            "java client <ip> <port number> 6 <authToken> <message_id>";
                    // Έλεγχος για το αν υπάρχει σωστός αριθμός ορισμάτων.
                    if (args.length < 5){
                        System.out.println("Invalid argument number.");
                        System.out.println(case6CorrectUsage);
                        System.exit(1);
                    }

                    // Λαμβάνω το authToken μετά από έλεγχο εγκυρότητας του ορίσματος που το περιέχει.
                    authToken = stringToIntWithErrorHandling(args[3],
                            String.format("%s%n%s", "Invalid authToken argument.", case6CorrectUsage));

                    // Λαμβάνω το username του παραλήπτη.
                    messageId = stringToIntWithErrorHandling(args[4],
                            String.format("%s%n%s", "Invalid message id argument.", case6CorrectUsage));


                    try {
                        // Εμφανίζω στον χρήστη την απάντηση του Server.
                        System.out.println(queriesToServer.deleteMessage(authToken, messageId));
                    } catch (InvalidAuthTokenException e){ // Χειρισμός άκυρου authToken.
                        System.out.println(e.getMessage());
                    }
                    break;
                default:
                    System.out.println("Given function id argument cannot be marched to implemented function.");
                    System.out.println(CORRECT_USAGE);
            }
        } catch (RemoteException | NotBoundException e) {
            System.out.println("A communication error with the server has occurred: " + e.getMessage());
        }
    }

    /**
     * Μετατρέπει ένα String σε ακέραιο όπου αν υπάρξει κάποιο σφάλμα κατά τη μετατροπή, εμφανίζει το δοθέν μήνυμα
     * messageIfFail και σταματάει την εκτέλεση του προγράμματος.
     * @param number Ο ακέραιος σε τύπο συμβολοσειράς.
     * @param messageIfFail ΤΟ μήνυμα που εμφανίζεται σε περίπτωση αποτυχίας.
     * @return Ο ακέραιος σε τύπο ακεραίου.
     */
    private static int stringToIntWithErrorHandling (String number, String messageIfFail){
        int trueNumber = 0;
        try {
            trueNumber = Integer.parseInt(number);
        } catch (NumberFormatException e){
            System.out.println(messageIfFail);
            System.exit(1);
        }
        return trueNumber;
    }
}
