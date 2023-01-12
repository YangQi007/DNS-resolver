import java.util.HashMap;

public class DNSCache {
    public HashMap<DNSQuestion, DNSRecord> Hashmap;

    public DNSCache() {
        Hashmap = new HashMap<>();
    }
    //check record in local cache
    public DNSRecord searchFor(DNSQuestion question) {
        if (Hashmap.containsKey(question)) {
            DNSRecord record = Hashmap.get(question);
            if (record.timestampValid()) {
                return record;
            } else {
                Hashmap.remove(question);
            }
        }
        return null;
    }
        //if record not in cache, add new record into cache
    public void addRecord(DNSQuestion question, DNSRecord record) {
        Hashmap.put(question, record);
    }
}
