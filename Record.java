public class Record {
    String field1, field2, field3, field4, field5;

    public Record(String field1, String field2, String field3, String field4, String field5) {
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
        this.field4 = field4;
        this.field5 = field5;
    }

    @Override
    public String toString() {
        return String.join(", ", field1, field2, field3, field4, field5);
    }
}