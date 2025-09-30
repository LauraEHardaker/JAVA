import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class Main {
    public static void main(String[] args) {
        try {
            CSVDatabase db = new CSVDatabase("users.csv");

            // Display existing records
            db.printAllRecords();

            // Add a new record
            db.addRecord(new Record("Alice", "30", "Engineer", "London", "UK"));

            // Save changes
            db.saveToFile();

            // Search example
            List<Record> results = db.searchByField1("Alice");
            System.out.println("Search results:");
            for (Record r : results) System.out.println(r);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}