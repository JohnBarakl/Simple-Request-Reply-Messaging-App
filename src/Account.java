import java.util.ArrayList;
import java.util.List;

/**
 * Μοντελοποιεί την οντότητα ενός λογαριασμού χρήστη που αποθηκεύεται στον εξυπηρετητή.
 *
 * @author Ioannis Baraklilis
 */
public class Account {
    /** Το όνομα χρήστη. Αποτελείται μόνο από αλφαριθμητικά και τον ειδικό χαρακτήρα “_”. */
    private String username;

    /** Ένας μοναδικός αριθμός αναγνώρισης του χρήστη (δημιουργείται από τον server και είναι προσωπικός/κρυφός). */
    private int authToken;

    /** Το γραμματοκιβώτιο του χρήστη, το οποίο είναι μία λίστα από Messages. */
    private List<Message> messageBox;

    /**
     * Ο προκαθορισμένος κατασκευαστής της Account που αρχικοποιεί τα πεδία σύμφωνα με τα ορίσματα.
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
     * Ο κατασκευαστής της Account που αρχικοποιεί τα πεδία σύμφωνα με τα ορίσματα αρχικοποιώντας το γραμματοκιβώτιο του
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

    public List<Message> getMessageBox() {
        return messageBox;
    }
}
