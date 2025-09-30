import java.io.*;
import java.util.*;

public class CSVDatabase {
    private List<Record> records = new ArrayList<>();
    private String filePath;

    public CSVDatabase(String filePath) throws IOException {
        this.filePath = filePath;
        loadFromFile();
    }

    private void loadFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine())!= null) {
                String[] fields = line.split(",");
                if (fields.length == 5) {
                    records.add(new Record(fields[0].trim(), fields[1].trim(), fields[2].trim(),
                                           fields[3].trim(), fields[4].trim()));
                }
            }
        }
    }

    public void saveToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Record r : records) {
                writer.write(String.join(",", r.field1, r.field2, r.field3, r.field4, r.field5));
                writer.newLine();
            }
        }
    }

    public void addRecord(Record record) { records.add(record); }
    public List<Record> searchByField1(String value) {
        List<Record> result = new ArrayList<>();
        for (Record r : records) {
            if (r.field1.equalsIgnoreCase(value)) result.add(r);
        }
        return result;
    }
    public void printAllRecords() {
        for (Record r : records) System.out.println(r);
    }
}


