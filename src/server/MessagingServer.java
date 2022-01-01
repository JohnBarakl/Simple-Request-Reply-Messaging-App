package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Μέσω αυτής της κλάσης υλοποιείται η επικοινωνία του εξυπηρετητή με τον χρήστη. <br>
 *
 * Ο εξυπηρετητής θα τρέχει συνεχώς ως υπηρεσία “ακούγοντας” για εισερχόμενες αιτήσεις από πελάτες. <br>
 * Κάθε εισερχόμενη αίτηση θα ανατίθεται σε διαφορετικό νήμα (αυτομάτως από τον μηχανισμό του RMI), και έτσι υπάρχει η
 * δυνατότητα να εξυπηρετηθούν πολλαπλές αιτήσεις ταυτόχρονα.
 *
 * @author Ioannis Baraklilis
 */
public class MessagingServer {
    /**
     * Η μέθοδος εκκίνησης της διεπαφής του εξυπηρετητή με το πρόγραμμα. <br>
     *
     * Με την κλήση της, αρχικοποιείται η λειτουργία του εξυπηρετητή είναι σε θέση να δεχθεί αιτήματα από πελάτες και
     * να ανταποκριθεί σε αυτά χρησιμοποιώντας τον μηχανισμό RMI της διεπαφής ClientQueries.
     *
     * @param args Ορίσματα κλήσης του μέρους της εφαρμογής του Server. <br>
     *             Αποτελείται μόνο από τον αριθμό του port στην οποία θα μπορεί να συνδεθεί ο χρήστης. <br>
     *             Άν δεν υπάρχει όρισμα ή αυτό δε βρίσκεται σε σωστή μορφή, εμφανίζεται κατάλληλο μήνυμα και γίνεται
     *             έξοδος από το πρόγραμμα. <br>
     *             Άν υπάρχουν άνω του ενός ορίσματα, λαμβάνεται υπόψιν μόνο το πρώτο και τα υπόλοιπα αγνοούνται.
     */
    public static void main(String[] args) {
        try {
            int portNumber;
            // Μετατρέπω το πρώτο όρισμα που αντιστοιχεί στον αριθμό του port του Server σε ακέραιο
            // κάνοντας παράλληλα κατάλληλους ελέγχους εγκυρότητας δεδομένων.
            try {
                if (args.length < 1 ){
                    System.out.println("No port argument provided.");
                    System.out.println("Correct use of server application call: java server <port number>");
                    return;
                }
                portNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e){
                System.out.println("Invalid port number argument.");
                System.out.println("Correct use of server application call: java server <port number>");
                return;
            }

            // Δημιουργώ αντικείμενο στο οποίο ικανοποιούνται τα αιτήματα των clients.
            // Μέσω αυτού, υπάρχει μία λίστα από λογαριασμούς (Account), όπου διατηρούνται τα δεδομένα τους όπως καταχωρημένοι χρήστες,
            // οι κωδικοί τους και τα γραμματοκιβώτια τους.
            ClientQueriesRemote clientQueries = new ClientQueriesRemote();

            // Δημιουργώ το RMI registry στο ζητούμενο port.
            Registry rmiRegistry = LocateRegistry.createRegistry(portNumber);

            // Καταχωρώ το clientQueries στο registry και αντιστοιχίζεται στο όνομα "client_query_point"
            //  ώστε να μπορεί ο χρήστης να έχει πρόσβαση στο αντικείμενο.
            rmiRegistry.rebind("client_query_point", clientQueries);
        } catch (RemoteException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
