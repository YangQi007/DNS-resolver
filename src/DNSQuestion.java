import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {
    public String [] qName;
    public short qType;
    public short qClass;
    //read a question from the input stream.
    public static DNSQuestion decodeQuestion(InputStream in, DNSMessage message) throws IOException {
        DNSQuestion q = new DNSQuestion();

        q.qName = message.readDomainName((ByteArrayInputStream) in);
        q.qType = new BigInteger(in.readNBytes(2)).shortValue();
        q.qClass = new BigInteger(in.readNBytes(2)).shortValue();

        return q;
    }

    //Write the question bytes which will be sent to the client. The hash map is used for us to compress the message
   public void writeBytes(ByteArrayOutputStream os, HashMap<String,Integer> domainNameLocations) throws IOException {

        DNSMessage.writeDomainName(os,domainNameLocations,qName);
        os.write(DNSMessage.shortToByteArr(qType));
        os.write(DNSMessage.shortToByteArr(qClass));
   }

   @Override
   public String toString(){
        return "DNSQuestion{" +
               " QName=" + Arrays.toString(qName) +
               ", QType=" + qType +
               ", QClass=" + qClass + "}";
   }

   @Override
    public boolean equals(Object o){
       if (this == o)
           return true;
       if (o == null)
           return false;
       if (getClass() != o.getClass())
           return false;
       DNSQuestion other = (DNSQuestion) o;
       if (qClass != other.qClass)
           return false;
       if (!Arrays.equals(qName, other.qName))
           return false;
       if (qType != other.qType)
           return false;
       return true;

   }

   @Override
    public int hashCode(){
       return  31 * Objects.hash(qType, qClass) + Arrays.hashCode(qName);

   }

}
