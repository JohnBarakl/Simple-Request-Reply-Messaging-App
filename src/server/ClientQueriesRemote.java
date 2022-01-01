package server;

import common.ClientQueries;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Η υλοποίηση της διεπαφής ClientQueries για τη χρήση RMI από μέρος του Server.
 *
 * @author Ioannis Baraklilis
 */
public class ClientQueriesRemote extends UnicastRemoteObject implements ClientQueries {
    /** Λίστα αποθηκευμένων λογαριασμών χρηστών. */
    private final List<Account> userAccounts;

    /** Υλοποιεί την αντιστοίχηση μοναδικού κωδικού με θέση λογαριασμού στη λίστα λογαριασμών userAccounts. */
    private final HashMap<Integer, Integer> userAuthTokenToAccount;

    /** Υλοποιεί την αντιστοίχηση username με  θέση λογαριασμού στη λίστα λογαριασμών userAccounts. */
    private final HashMap<String, Integer> usernameToAccount;

    /** Αποθηκεύει το σύνολο των χρησιμοποιούμενων μοναδικών κλειδιών λογαριασμών */
    private final HashSet<Integer> usedAuthTokens;

    /** Η γεννήτρια τυχαίων αριθμών που θα χρησιμοποιηθεί για τη δημιουργία μοναδικών κωδικών */
    private final Random randomGenerator;

    /**
     * Ο προκαθορισμένος κατασκευαστής
     * @throws RemoteException Σε περίπτωση αποτυχίας εξαγωγής αντικειμένου.
     */
    protected ClientQueriesRemote() throws RemoteException {
        super();
        userAccounts = new ArrayList<>();
        userAuthTokenToAccount = new HashMap<>();
        usernameToAccount = new HashMap<>();
        randomGenerator = new Random();
        usedAuthTokens = new HashSet<>();
    }

    /**
     * Δημιουργεί και επιστρέφει νέο (τυχαίο) μοναδικό κωδικό αυθεντικοποίησης χρήστη εξασφαλίζοντας ότι δεν έχει ήδη δεσμευτεί
     * από άλλον χρήστη χρησιμοποιώντας το πεδίο usedAuthTokens. <br>
     *
     * Για τη δημιουργία τυχαίων αριθμών χρησιμοποιείται η γεννήτρια randomGenerator. Η εξασφάλιση παραγωγής μη δεσμευμένου
     * authToken υπόκειται στη συνεχή παραγωγή νέων τυχαίων αριθμών μέχρι να βρεθεί μη δεσμευμένο. Διαφορετικά, η διαδικασία
     * αυτή θα συνεχίζεται επ' αόριστον μέχρις ότου βρεθεί αχρησιμοποίητο κλειδί. <br>
     *
     * Σημείωση: Η ευθύνη καταγραφής του αριθμού ως δεσμευμένου στο usedAuthTokens είναι ευθύνη της μεθόδου που χρησιμοποιεί
     * την παρούσα.
     * @return Νέος μη χρησιμοποιούμενος τυχαίος μοναδικός κωδικός χρήστη.
     */
    private int generateUniqueAuthToken(){
        int tempNumber = randomGenerator.nextInt();

        // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
        synchronized (this){
            // Συνεχή παραγωγή τυχαίων αριθμών μέχρις ότου βρεθεί κάποιος που δεν είναι εγγεγραμμένος.
            // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
            while (usedAuthTokens.contains(tempNumber)) {
                tempNumber = randomGenerator.nextInt();
            }
        }
        return tempNumber;
    }

    /**
     * Επιστρέφει αναφορά στο αντικείμενο λογαριασμού με μοναδικό κωδικό authToken.
     * @param authToken Ο μοναδικός κωδικός που πρέπει να αντιστοιχεί στον λογαριασμό που αναζητείται.
     * @return Ο λογαριασμός που αναζητείται.
     * @throws IllegalArgumentException Σε περίπτωση που ο λογαριασμός που αναζητείται δεν υπάρχει.
     */
    private Account getValidUser(int authToken) throws IllegalArgumentException {
        Account thisAccount;
        // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
        synchronized (this) {
            thisAccount = userAccounts.get(userAuthTokenToAccount.get(authToken));
        }

        // TODO: remove if q1 false
        // Σε περίπτωση που το authToken όρισμα δεν αντιστοιχεί σε λογαριασμό, επιστρέφω exception.
        if (thisAccount == null) {
            throw new IllegalArgumentException("authToken does not correspond to valid account.");
        }
        return thisAccount;
    }

    /**
     * Δημιουργεί ένα account για το user και χρησιμοποιεί το δοσμένο username.
     *
     * @param username Το username του νέου χρήστη που θα δημιουργηθεί.
     * @return Μοναδικός κωδικός (token) ο οποίος χρησιμοποιείται για να
     * αυθεντικοποιηθεί ο χρήστης στα επόμενα αιτήματα του. <br>
     * Σε περίπτωση που το όνομα χρήστη ήδη χρησιμοποιείται από άλλον χρήστη,
     * επιστρέφεται "Sorry, the user already exists". <br>
     * Σε περίπτωση που το username έχει λάθος μορφή (πρέπει να Αποτελείται μόνο από
     * αλφαριθμητικά και τον ειδικό χαρακτήρα "_"), πρέπει να επιστρέφεται
     * "Invalid Username".
     */
    @Override
    public String createAccount(String username) throws RemoteException {
        // Έλεγχος ορθότητας μορφής username.
        if (username==null || !username.matches("[a-zA-Z0-9_]+")){
            return "Invalid Username";
        }

        // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
        synchronized (this){
            // Έλεγχος για το αν υπάρχει χρήστης με το ίδιο όνομα.
            if (usernameToAccount.containsKey(username)){
                return "Sorry, the user already exists";
            }

            // Δημιουργία νέου λογαριασμού και εγγραφή του στις δομές δεδομένων του προγράμματος.
            int newAuthToken = generateUniqueAuthToken();
            Account newAccount = new Account(username, newAuthToken, new ArrayList<>());
            usedAuthTokens.add(newAuthToken); // Σημείωση του id ως δεσμευμένο.
            userAccounts.add(newAccount); // Προσθήκη του νέου λογαριασμού στη λίστα.
            int newAccountPlace = userAccounts.size()-1; // Εύρεση της θέσης του νέου λογαριασμού στη λίστα.
            userAuthTokenToAccount.put(newAuthToken, newAccountPlace); // Δημιουργία αντιστοίχησης authToken-λογαριασμού.
            usernameToAccount.put(username, newAccountPlace); // Δημιουργία αντιστοίχησης username-λογαριασμού.

            return String.valueOf(newAuthToken);
        }
    }

    /**
     * Δείχνει μια λίστα με όλα τα accounts που υπάρχουν στο σύστημα.
     *
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @return Η λίστα με το username όλων των account.
     */
    @Override
    public String[] showAccounts(int authToken) throws RemoteException {
        // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
        synchronized (this){
            return usernameToAccount.keySet().toArray(new String[0]);
        }
    }

    /**
     * Στέλνει το μήνυμα messageBody στο account με username recipient.
     *
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του αποστολέα.
     * @param recipient Το username του παραλήπτη.
     * @param messageBody Το περιεχόμενο του μηνύματος.
     * @return Κατάσταση αποστολής του μηνύματος.
     */
    @Override
    public String sendMessage(int authToken, String recipient, String messageBody) throws RemoteException {
        // Βρίσκω τον λογαριασμό του χρήστη που στέλνει το μήνυμα.
        Account thisUser = getValidUser(authToken);

        // Βρίσκω τον λογαριασμό του χρήστη που λαμβάνει το μήνυμα.
        Account recipientAccount;

        // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
        synchronized (this) {
            recipientAccount = userAccounts.get(usernameToAccount.get(recipient));
        }

        // Έλεγχος για το αν το προφίλ του χρήστη παραλήπτη υπάρχει.
        if (recipientAccount == null) {
            return "User does not exist";
        }

        // Προσθήκη μηνύματος στο γραμματοκιβώτιο παραλήπτη.
        recipientAccount.addMessageInMessageBox(thisUser.getUsername(), messageBody);

        return "OK";
    }

    /**
     * Εμφανίζει τη λίστα με όλα τα μηνύματα για έναν συγκεκριμένο χρήστη.
     *
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @return Λίστα με όλα τα μηνύματα του χρήστη. <br>
     * Για κάθε στοιχείο της λίστας, εμφανίζεται ο μοναδικός κωδικός του μηνύματος,
     * το username αποστολέα και η κατάσταση για το αν έχει ήδη διαβαστεί.
     */
    @Override
    public String[] showInbox(int authToken) throws RemoteException {
        // Βρίσκω τον λογαριασμό του ζητούμενου χρήστη.
        Account thisUser = getValidUser(authToken);

        // Προσωρινή λίστα που αποθηκεύει τα ενδιάμεσα αποτελέσματα που τελικά θα επιστραφούν στον client.
        ArrayList<String> queryResults = new ArrayList<>();

        // Για κάθε μήνυμα, επεκτείνω τη λίστα με τα αποτελέσματα.
        for (Message m : thisUser.getMessageBoxContents()) {
            queryResults.add(String.format("%d. from: %s%s", m.getId(), m.getSender(), m.isRead() ? "" : "*"));
        }

        return queryResults.toArray(new String[0]);
    }

    /**
     * Αυτή η λειτουργία επιστρέφει το περιεχόμενο ενός μηνύματος του χρήστη με id messageId.
     * Έπειτα το μήνυμα (αν υπάρχει) σημειώνεται ως διαβασμένο.
     * Αν το μήνυμα δεν υπάρχει, επιστρέφεται "Message ID does not exist".
     *
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @param messageId Ο μοναδικός κωδικός μηνύματος προς ανάγνωση.
     * @return Το περιεχόμενο του μηνύματος, αν υπάρχει. <br>
     * Αν το μήνυμα δεν υπάρχει, επιστρέφεται "server.Message ID does not exist".
     */
    @Override
    public String readMessage(int authToken, int messageId) throws RemoteException {
        // Βρίσκω τον λογαριασμό του ζητούμενου χρήστη, σημειώνω το μήνυμα ως διαβασμένο και επιστρέφω το περιεχόμενο του.
        return getValidUser(authToken).readMessage(messageId);
    }

    /**
     * Αυτή η λειτουργία διαγράφει το μήνυμα με id messageId.
     *
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @param messageId Ο μοναδικός κωδικός μηνύματος προς διαγραφή.
     * @return "OK", σε περίπτωση επιτυχίας ή "server.Message does not exist" αν το μήνυμα δεν υπάρχει.
     */
    @Override
    public String deleteMessage(int authToken, int messageId) throws RemoteException {
        // Βρίσκω τον λογαριασμό του ζητούμενου χρήστη, επιχειρώ να το διαγράψω και επιστρέφω τα αποτελέσματα της διαγραφής.
        return getValidUser(authToken).deleteMessage(messageId);
    }
}
