import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

/*Everything after the header and question parts of the DNS message are stored as records.
This should have all the fields listed in the spec as well as a Date object storing when this record was created by your program.
 **/
public class DNSRecord {
    String [] name;
    short type;
    short class_;
    int ttl;
    short rdLength;
    byte [] rData;
    private Calendar time;

    public static DNSRecord decodeRecord(ByteArrayInputStream in, DNSMessage message) throws IOException {
        DNSRecord record = new DNSRecord();
        record.name = message.readDomainName(in);
        record.type = new BigInteger(in.readNBytes(2)).shortValue();
        record.class_ = new BigInteger(in.readNBytes(2)).shortValue();
        record.ttl = new BigInteger(in.readNBytes(4)).shortValue();
        record.rdLength = new BigInteger(in.readNBytes(2)).shortValue();

        record.rData = new byte[record.rdLength];
        for (int i = 0; i < record.rdLength; i++){
            record.rData[i] = (byte) in.read();
        }

        record.time = Calendar.getInstance();
        record.time.add(Calendar.SECOND, record.ttl);

        return record;
    }

    public void writeBytes(ByteArrayOutputStream os, HashMap<String, Integer> domainNameLocations) throws IOException {
        //write name
        DNSMessage.writeDomainName(os,domainNameLocations,name);
        //write type
        os.write(DNSMessage.shortToByteArr(type));
        //write class
        os.write(DNSMessage.shortToByteArr(class_));
        //write ttl
        byte[] ttlArr = new byte[4];
        for (int i = 0; i < 4; i++){
            ttlArr[i] = (byte) (ttl >> (8 * (3 - i)));
        }
        for (byte ttlByte : ttlArr) {
            os.write(ttlByte);
        }
        // write length
        os.write(DNSMessage.shortToByteArr(rdLength));
        // write data
        for (byte data: rData) {
            os.write(data);
        }

    }

   public boolean timestampValid(){
       Calendar newTime = Calendar.getInstance();
       return newTime.before(time);
    }

    @Override
    public String toString(){
        return "DNSRecord {name=" + Arrays.toString(name) + ", type=" + type + ", class0=" + class_ + ", ttl=" + ttl
                + ", rdLength=" + rdLength + ", rData=" + Arrays.toString(rData) + ", deathDate=" + time + "}";
    }

}
