package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Μοντελοποιεί την οντότητα ενός λογαριασμού χρήστη που αποθηκεύεται στον εξυπηρετητή.
 *
 * @author Ioannis Baraklilis
 */
public class Account implements Serializable {
    /** Το όνομα χρήστη. Αποτελείται μόνο από αλφαριθμητικά και τον ειδικό χαρακτήρα “_”. */
    private String username;

    /** Ένας μοναδικός αριθμός αναγνώρισης του χρήστη (δημιουργείται από τον server και είναι προσωπικός/κρυφός). */
    private int authToken;

    /** Το γραμματοκιβώτιο του χρήστη, το οποίο είναι μία λίστα από Messages. */
    private List<Message> messageBox;

    /**
     * Ο προκαθορισμένος κατασκευαστής της server.Account που αρχικοποιεί τα πεδία σύμφωνα με τα ορίσματα.
     * @param username Το όνομα χρήστη. Αποτελείται μόνο από αλφαριθμητικά και τον ειδικό χαρακτήρα “_”.
     * @param authToken Ένας μοναδικός αριθμός αναγνώρισης του χρήστη (δημιουργείται από τον server και είναι προσωπικός/κρυφός).
     * @param messageBox Το γραμματοκιβώτιο του χρήστη, το οποίο είναι μία λίστα από Messages. <br>
     *                   Άν είναι null, το γραμματοκιβώτιο του χρήστη αρχικοποιείται με μία κενή λίστα.
     */
    public Account(String username, int authToken, List<Message> messageBox) {
        // Έλεγχος για το αν το όνομα δόθηκε ως null ή αποτελείται μόνο από αλφαριθμητικά και τον ειδικό χαρακτήρα "_".
        if (username == null){
            throw new IllegalArgumentException("The username argument must not be null.");
        } else if (!username.matches("[a-zA-Z0-9_]+")){
            throw new IllegalArgumentException("A username must be a string that consists of characters that are " +
                    "alphanumeric or _.");
        }
        this.username = username;

        this.authToken = authToken;

        if (messageBox != null) {
            this.messageBox = messageBox;
        } else {
            this.messageBox = new ArrayList<>();
        }
    }
    /**
     * Ο κατασκευαστής της server.Account που αρχικοποιεί τα πεδία σύμφωνα με τα ορίσματα αρχικοποιώντας το γραμματοκιβώτιο του
     * χρήστη με μία κενή λίστα.
     * @param username Το όνομα χρήστη. Αποτελείται μόνο από αλφαριθμητικά και τον ειδικό χαρακτήρα “_”.
     * @param authToken Ένας μοναδικός αριθμός αναγνώρισης του χρήστη (δημιουργείται από τον server και είναι προσωπικός/κρυφός).
     */
    public Account(String username, int authToken) {
        // Έλεγχος για το αν το όνομα δόθηκε ως null ή αποτελείται μόνο από αλφαριθμητικά και τον ειδικό χαρακτήρα "_".
        if (username == null){
            throw new IllegalArgumentException("The username argument must not be null.");
        } else if (!username.matches("[a-zA-Z0-9_]+")){
            throw new IllegalArgumentException("A username must be a string that consists of characters that are " +
                    "alphanumeric or _.");
        }
        this.username = username;

        this.authToken = authToken;
        this.messageBox = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public int getAuthToken() {
        return authToken;
    }

    /**
     * Επιστρέφει αντίγραφο της λίστας με αντίγραφα των μηνυμάτων που υπάρχουν στο γραμματοκιβώτιο του χρήστη.
     * @return Αντίγραφο λίστας μηνυμάτων του γραμματοκιβωτίου.
     */
    public synchronized Message[] getMessageBoxContents() {
        // Αρχικοποιώ το "δοχείο" με τα μηνύματα.
        Message[] messageCopies = new Message[messageBox.size()];

        for (int i = 0, end = messageBox.size(); i < end; i++){
            messageCopies[i] = new Message(messageBox.get(i));
        }

        return messageCopies;
    }

    /**
     * Δημιουργεί και προσθέτει νέο μήνυμα με αποστολέα senderUsername και περιεχόμενο messageBody
     * στο γραμματοκιβώτιο του χρήστη.
     * @param senderUsername Ο αποστολέας του μηνύματος.
     * @param messageBody Το περιεχόμενο του μηνύματος.
     */
    public synchronized void addMessageInMessageBox(String senderUsername, String messageBody){
        messageBox.add(new Message(false, senderUsername, this.username, messageBody));
    }

    /**
     * Επιστρέφει τον αποστολέα και το περιεχόμενο ενός μηνύματος του χρήστη με id messageId.
     * Έπειτα το μήνυμα (αν υπάρχει) σημειώνεται ως διαβασμένο.
     * Αν το μήνυμα δεν υπάρχει, επιστρέφεται "Message ID does not exist".
     *
     * @param messageId Το id του μηνύματος που αναζητείται.
     * @return Ο αποστολέας και το περιεχόμενο του μηνύματος αν αυτό βρεθεί ή, σε διαφορετική περίπτωση, το αντίστοιχο μήνυμα λάθους.
     */
    public synchronized String readMessage(int messageId){
        Message targetMessage = null;

        // Αναζήτηση για μήνυμα με id messageId.
        for (Message m : messageBox) {
            if (m.getId() == messageId) {
                targetMessage = m;
                break; // Το μήνυμα βρέθηκε, τέλος αναζήτησης.
            }
        }

        // Αν η αναζήτηση ολοκληρώθηκε ανεπιτυχώς το targetMessage θα έχει την αρχικοποιημένη τιμή null.
        if (targetMessage == null) {
            return "Message ID does not exist";
        } else { // Το μήνυμα βρέθηκε. Αποθηκεύω το περιεχόμενο επιστροφής και το σημειώνω ώς διαβασμένο.
            targetMessage.setRead(true);
            return String.format("(%s) %s", targetMessage.getSender(), targetMessage.getBody());
        }
    }

    /**
     * Διαγράφει το μήνυμα του χρήστη με id messageId, άν υπάρχει.
     * Αν το μήνυμα δεν υπάρχει, επιστρέφεται "Message ID does not exist".
     *
     * @param messageId Το id του μηνύματος που αναζητείται.
     * @return H κατάσταση επιτυχίας της διαγραφής. <br>
     *         "OK" για επιτυχία και "Message does not exist" σε περίπτωση αποτυχίας εύρεσης μηνύματος.
     */
    public synchronized String deleteMessage(int messageId){
        Message targetMessage = null;

        // Αναζήτηση για μήνυμα με id messageId.
        for (int i = 0, end = messageBox.size(); i < end; i++) {
            if (messageBox.get(i).getId() == messageId) {
                messageBox.remove(i);

                // Το μήνυμα βρέθηκε και διαγράφηκε επιτυχώς, τέλος μεθόδου και επιστροφή επιτυχούς αποτελέσματος.
                return "OK";
            }
        }

       return "Message does not exist";
    }
}
