/**
 * Μοντελοποιεί την οντότητα ενός μηνύματος που μπορεί να αποστέλλεται, παραλαμβάνεται ή να είναι αποθηκευμένο στον
 * εξυπηρετητή.
 *
 * @author Ioannis Baraklilis
 */
public class Message {
    private static int idCounter = -1;

    /** Υποδεικνύει αν το μήνυμα έχει ήδη διαβαστεί. */
    private boolean isRead;

    /** Ο αποστολέας του μηνύματος. */
    private String sender;

    /** Ο παραλήπτης του μηνύματος. */
    private String receiver;

    /** Το κείμενο του μηνύματος. */
    private String body;

    // TODO: Document following fields.
    /** Ο μοναδικός κωδικός που αντιστοιχεί σε κάθε μήνυμα. */
    private int id;

    /** Ένας μοναδικός αριθμός αναγνώρισης του αποστολέα */
    private int senderId;

    /** Ένας μοναδικός αριθμός αναγνώρισης του παραλήπτη */
    private int receiverId;

    /**
     * Ο προκαθορισμένος κατασκευαστής της Message που αρχικοποιεί τα πεδία σύμφωνα με τα ορίσματα.
     * @param isRead Υποδεικνύει αν το μήνυμα έχει ήδη διαβαστεί.
     * @param sender Ο αποστολέας του μηνύματος.
     * @param receiver Ο παραλήπτης του μηνύματος.
     * @param body Το κείμενο του μηνύματος.
     */
    public Message(boolean isRead, String sender, String receiver, String body) {
        this.isRead = isRead;

        // Ο αποστολέας και παραλήπτης δεν επιτρέπεται να έχει δοθεί ως όρισμα το null.
        if (sender == null || receiver == null){
            throw new IllegalArgumentException("The sender and receiver arguments must not be null.");
        }
        this.sender = sender;
        this.receiver = receiver;

        this.body = body;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getBody() {
        return body;
    }
}
