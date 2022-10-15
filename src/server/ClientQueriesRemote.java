package server;

import common.ClientQueries;
import common.InvalidAuthTokenException;

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

    /** Υλοποιεί την αντιστοίχηση username με θέση λογαριασμού στη λίστα λογαριασμών userAccounts. */
    private final HashMap<String, Integer> usernameToAccount;

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
    }

    /**
     * Δημιουργεί και επιστρέφει νέο (τυχαίο) μοναδικό κωδικό αυθεντικοποίησης χρήστη εξασφαλίζοντας ότι δεν έχει ήδη δεσμευτεί
     * από άλλον χρήστη χρησιμοποιώντας το πεδίο userAuthTokenToAccount. <br>
     *
     * Για τη δημιουργία τυχαίων αριθμών χρησιμοποιείται η γεννήτρια randomGenerator. Η εξασφάλιση παραγωγής μη δεσμευμένου
     * authToken υπόκειται στη συνεχή παραγωγή νέων τυχαίων αριθμών μέχρι να βρεθεί μη δεσμευμένο. Διαφορετικά, η διαδικασία
     * αυτή θα συνεχίζεται επ' αόριστον μέχρις ότου βρεθεί αχρησιμοποίητο κλειδί. <br>
     *
     * Σημείωση: Η ευθύνη καταγραφής του αριθμού ως δεσμευμένου στο userAuthTokenToAccount (χρήση του ως κλειδί) είναι ευθύνη της μεθόδου που χρησιμοποιεί
     * την παρούσα.
     * @return Νέος (μη χρησιμοποιούμενος) τυχαίος μοναδικός κωδικός χρήστη.
     */
    private int generateUniqueAuthToken(){
        int tempNumber ;

        // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
        synchronized (this){
            tempNumber = randomGenerator.nextInt();
            tempNumber = tempNumber>=0?tempNumber:-tempNumber;

            // Συνεχή παραγωγή τυχαίων αριθμών μέχρις ότου βρεθεί κάποιος που δεν είναι εγγεγραμμένος.
            while (userAuthTokenToAccount.containsKey(tempNumber)) {
                tempNumber = randomGenerator.nextInt();
                tempNumber = tempNumber>=0?tempNumber:-tempNumber;
            }
        }

        return tempNumber;
    }

    /**
     * Επιστρέφει αναφορά στο αντικείμενο λογαριασμού με μοναδικό κωδικό authToken.
     * Αν δε βρεθεί χρήστης με τέτοιον κωδικό επιστρέφεται null.
     * @param authToken Ο μοναδικός κωδικός που πρέπει να αντιστοιχεί στον λογαριασμό που αναζητείται.
     * @return Ο λογαριασμός που αναζητείται.
     */
    private Account getValidUser(int authToken){
        Account thisAccount;
        // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
        synchronized (this) {
            Integer userAccountsPosition = userAuthTokenToAccount.get(authToken);
            if (userAccountsPosition == null) // Ελέγχω για την εγκυρότητα του authToken
                return null;

            thisAccount = userAccounts.get(userAccountsPosition);
        }

        return thisAccount;
    }

    /**
     * Ελέγχει την εγκυρότητα του token του ορίσματος ως token που αντιστοιχεί σε αποθηκευμένο χρήστη και επιστρέφει το αποτέλεσμα.
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη, του οποίου η ύπαρξη επαληθεύεται.
     * @return Η κατάσταση επιτυχίας επαλήθευσης.
     */
    private boolean checkTokenValidity(int authToken){
        boolean valid = false;

        synchronized (this) {
            valid = userAuthTokenToAccount.containsKey(authToken);
        }

        return valid;
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
        if (username==null || !username.matches("(\\p{IsAlphabetic}|[0-9]|_)+")){
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
     * @throws InvalidAuthTokenException Σε περίπτωση πού το authToken δεν αντιστοιχεί σε χρήστη.
     */
    @Override
    public String[] showAccounts(int authToken) throws RemoteException, InvalidAuthTokenException {
        if (!checkTokenValidity(authToken)){
            throw new InvalidAuthTokenException();
        }

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
     * @throws InvalidAuthTokenException Σε περίπτωση πού το authToken δεν αντιστοιχεί σε χρήστη.
     */
    @Override
    public String sendMessage(int authToken, String recipient, String messageBody) throws RemoteException, InvalidAuthTokenException {
        if (!checkTokenValidity(authToken)){
            throw new InvalidAuthTokenException();
        }

        // Βρίσκω τον λογαριασμό του χρήστη που στέλνει το μήνυμα.
        Account thisUser = getValidUser(authToken);

        // Βρίσκω τον λογαριασμό του χρήστη που λαμβάνει το μήνυμα.
        Account recipientAccount;

        // Εξασφάλιση συγχρονισμού μεθόδου: Εισάγω το κρίσιμο τμήμα εντός synchronized block.
        synchronized (this) {
            Integer accountPosition = usernameToAccount.get(recipient);
            // Ο παραλήπτης μπορεί να μην υπάρχει, έλεγχος για το αν υπάρχει.
            if (accountPosition != null){
                recipientAccount = userAccounts.get(accountPosition);
            } else {
                recipientAccount = null;
            }
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
     *         Για κάθε στοιχείο της λίστας, εμφανίζεται ο μοναδικός κωδικός του μηνύματος,
     *         το username αποστολέα και η κατάσταση για το αν έχει ήδη διαβαστεί. <br>
     * @throws InvalidAuthTokenException Σε περίπτωση πού το authToken δεν αντιστοιχεί σε χρήστη.
     */
    @Override
    public String[] showInbox(int authToken) throws RemoteException, InvalidAuthTokenException {
        if (!checkTokenValidity(authToken)){
            throw new InvalidAuthTokenException();
        }

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
     *         Αν το μήνυμα δεν υπάρχει, επιστρέφεται "Message ID does not exist".
     * @throws InvalidAuthTokenException Σε περίπτωση πού το authToken δεν αντιστοιχεί σε χρήστη.
     */
    @Override
    public String readMessage(int authToken, int messageId) throws RemoteException, InvalidAuthTokenException {
        if (!checkTokenValidity(authToken)){
            throw new InvalidAuthTokenException();
        }

        // Βρίσκω τον λογαριασμό του ζητούμενου χρήστη, σημειώνω το μήνυμα ως διαβασμένο και επιστρέφω το περιεχόμενο του.
        return getValidUser(authToken).readMessage(messageId);
    }

    /**
     * Αυτή η λειτουργία διαγράφει το μήνυμα με id messageId.
     *
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @param messageId Ο μοναδικός κωδικός μηνύματος προς διαγραφή.
     * @return "OK", σε περίπτωση επιτυχίας ή "Message ID does not exist" αν το μήνυμα δεν υπάρχει.
     * @throws InvalidAuthTokenException Σε περίπτωση πού το authToken δεν αντιστοιχεί σε χρήστη.
     */
    @Override
    public String deleteMessage(int authToken, int messageId) throws RemoteException, InvalidAuthTokenException {
        if (!checkTokenValidity(authToken)){
            throw new InvalidAuthTokenException();
        }

        // Βρίσκω τον λογαριασμό του ζητούμενου χρήστη, επιχειρώ να το διαγράψω και επιστρέφω τα αποτελέσματα της διαγραφής.
        return getValidUser(authToken).deleteMessage(messageId);
    }
}
