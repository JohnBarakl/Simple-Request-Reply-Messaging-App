package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Περιγράφει τα αιτήματα ενός client προς έναν server χρησιμοποιώντας RMI.<br>
 *
 * Κάθε μέθοδος της κλάσης αυτής περιγράφει ένα συγκεκριμένο αίτημα ώστε να μπορέσει να
 *  εκτελέσει την (αντίστοιχη) ζητούμενη λειτουργία.
 *
 * @author Ioannis Baraklilis
 */
public interface ClientQueries extends Remote {
    /**
     * Δημιουργεί ένα account για το user και χρησιμοποιεί το δοσμένο username.
     * @param username Το username του νέου χρήστη που θα δημιουργηθεί.
     * @return Μοναδικός κωδικός (token) ο οποίος χρησιμοποιείται για να
     *          αυθεντικοποιηθεί ο χρήστης στα επόμενα αιτήματα του. <br>
     *         Σε περίπτωση που το όνομα χρήστη ήδη χρησιμοποιείται από άλλον χρήστη,
     *          επιστρέφεται "Sorry, the user already exists". <br>
     *         Σε περίπτωση που το username έχει λάθος μορφή (πρέπει να Αποτελείται μόνο από
     *          αλφαριθμητικά και τον ειδικό χαρακτήρα "_"), πρέπει να επιστρέφεται
     *          "Invalid Username".
     */
    public String createAccount(String username) throws RemoteException;

    /**
     * Δείχνει μια λίστα με όλα τα accounts που υπάρχουν στο σύστημα.
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @return Η λίστα με το username όλων των account.
     */
    public String[] showAccounts(int authToken) throws RemoteException;

    /**
     * Στέλνει το μήνυμα messageBody στο account με username recipient.
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του αποστολέα.
     * @param recipient Το username του παραλήπτη.
     * @param messageBody Το περιεχόμενο του μηνύματος.
     * @return Κατάσταση αποστολής του μηνύματος.
     */
    public String sendMessage(int authToken, String recipient, String messageBody) throws RemoteException;

    /**
     * Εμφανίζει τη λίστα με όλα τα μηνύματα για έναν συγκεκριμένο χρήστη.
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @return Λίστα με όλα τα μηνύματα του χρήστη. <br>
     *         Για κάθε στοιχείο της λίστας, εμφανίζεται ο μοναδικός κωδικός του μηνύματος,
     *          το username αποστολέα και η κατάσταση για το αν έχει ήδη διαβαστεί.
     */
    public String[] showInbox(int authToken) throws RemoteException;

    /**
     * Αυτή η λειτουργία επιστρέφει το περιεχόμενο ενός μηνύματος του χρήστη με id
     * messageId. Έπειτα το μήνυμα σημειώνεται ως διαβασμένο.
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @param messageId Ο μοναδικός κωδικός μηνύματος προς ανάγνωση.
     * @return Το περιεχόμενο του μηνύματος, αν υπάρχει. <br>
     *         Αν το μήνυμα δεν υπάρχει, επιστρέφεται "server.Message ID does not exist".
     */
    public String readMessage(int authToken, int messageId) throws RemoteException;

    /**
     * Αυτή η λειτουργία διαγράφει το μήνυμα με id messageId.
     * @param authToken Ο μοναδικός κωδικός αυθεντικοποίησης του χρήστη.
     * @param messageId Ο μοναδικός κωδικός μηνύματος προς διαγραφή.
     * @return "OK", σε περίπτωση επιτυχίας ή "server.Message does not exist" αν το μήνυμα δεν υπάρχει.
     */
    public String deleteMessage(int authToken, int messageId) throws RemoteException;
}