package br.com.dvlima;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * https://firebase.google.com/docs/database/admin/save-data?hl=pt-br
 * Created by danilo.fernandes on 27/02/2018.
 */
public class FirebaseSaveObject {

    private static FirebaseDatabase firebaseDatabase;
    private static final String ITEMS_TABLE = "ITENS";
    private static final String GOOGLE_CREDENTIALS = "path/to/serviceAccountCredentials.json";

    public static void main(String[] args) {
        List<Item> items = new ArrayList<>();

        for (long i = 0; i < 300; i++) {
            items.add(new Item(i, "Item number " + i, 0.0d));
        }

        initFirebase();
        FirebaseSaveObject app = new FirebaseSaveObject();
        app.list();
        app.save(items, ITEMS_TABLE);
    }


    /**
     * initialize firebase.
     */
    private static void initFirebase() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(new File(GOOGLE_CREDENTIALS))))
                    .setDatabaseUrl("https://<databaseName>.firebaseio.com/")
                    .build();

            FirebaseApp.initializeApp(options);
            firebaseDatabase = FirebaseDatabase.getInstance();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void list() {
        DatabaseReference databaseReference = firebaseDatabase.getReference("/" + ITEMS_TABLE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object document = dataSnapshot.getValue();
                System.out.println(document);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void save(List<?> lista, String table) {
        if (lista != null) {
            DatabaseReference childReference = firebaseDatabase.getReference("/").child(table);
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            childReference.setValue(lista, (DatabaseError de, DatabaseReference dr) -> {
                System.out.println("Collection saved!");
                countDownLatch.countDown();
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
