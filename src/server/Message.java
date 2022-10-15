package server;

import java.io.Serializable;

/**
 * Μοντελοποιεί την οντότητα ενός μηνύματος που μπορεί να αποστέλλεται, παραλαμβάνεται ή να είναι αποθηκευμένο στον
 * εξυπηρετητή.
 *
 * @author Ioannis Baraklilis
 */
public class Message implements Serializable {
    /** Μετρητής μοναδικού κωδικού μηνυμάτων. */
    private static int idCounter = -1;

    /** Υποδεικνύει αν το μήνυμα έχει ήδη διαβαστεί. */
    private boolean isRead;

    /** Ο αποστολέας του μηνύματος. */
    private String sender;

    /** Ο παραλήπτης του μηνύματος. */
    private String receiver;

    /** Το κείμενο του μηνύματος. */
    private String body;

    /** Ο μοναδικός κωδικός που αντιστοιχεί σε κάθε μήνυμα. */
    private int id;

    /**
     * Ο προκαθορισμένος κατασκευαστής της Message που αρχικοποιεί τα πεδία σύμφωνα με τα ορίσματα.
     * @param isRead Υποδεικνύει αν το μήνυμα έχει ήδη διαβαστεί.
     * @param sender Ο αποστολέας του μηνύματος.
     * @param receiver Ο παραλήπτης του μηνύματος.
     * @param body Το κείμενο του μηνύματος.
     */
    public Message(boolean isRead, String sender, String receiver, String body) {
        id = ++idCounter;

        this.isRead = isRead;

        // Ο αποστολέας και παραλήπτης δεν επιτρέπεται να έχει δοθεί ως όρισμα το null.
        if (sender == null || receiver == null){
            throw new IllegalArgumentException("The sender and receiver arguments must not be null.");
        }
        this.sender = sender;
        this.receiver = receiver;

        this.body = body;
    }

    /**
     * Κατασκευαστής αντιγράφων αντικειμένων Message.
     * @param m Αντικείμενο που θα αντιγραφεί.
     */
    public Message(Message m){
        if (m==null){
            throw new IllegalArgumentException("Message m must be non-null.");
        }

        this.id = m.id;
        this.isRead = m.isRead;
        this.sender = m.sender;
        this.receiver = m.receiver;
        this.body = m.body;
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

    public int getId() {
        return id;
    }

    public void setRead(boolean read) {
        isRead = read;
    }


}
