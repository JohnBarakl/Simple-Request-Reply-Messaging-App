package common;

/**
 * Σηματοδοτεί ότι κάποιο token που δόθηκε δεν είναι έγκυρο, δηλαδή δεν αντιστοιχεί σε εγγεγραμμένο χρήστη.
 */
public class InvalidAuthTokenException extends Exception {

    /**
     * Ο προκαθορισμένος κατασκευαστής της κλάσης.
     * Απλά τροφοδοτεί το αντίστοιχο μήνυμα ως μήνυμα Exception.
     */
    public InvalidAuthTokenException(){
        super("Invalid Auth Token");
    }
}
