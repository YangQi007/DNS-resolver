import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/*The header contains the following fields:

                                    1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ARCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

 */
public class DNSHeader {
    public byte[] id; // A 16 bit identifier
    public byte qr; // A 1 bit field specifies whether this message is a query(0), or a response(1)
    public byte opcode; // A 4 bit field that specifies kind of query in this message
    public byte  aa; // Authoritative Answer
    public byte  tc; // TrunCation
    public byte  rd; // Recursion Desired
    public byte  ra; // Recursion Available
    public byte  z; // Reserved for future use
    public byte  rcode; // Response Code 4 bit
    public short  qdCount; // An unsigned 16 bit integer specifying the number of entries in question section
    public short anCount; // An unsigned 16 bit integer specifying the number of resource in answer section
    public short  nsCount; // An unsigned 16 bit integer specifying the number of name server resource in authority records
    public short arCount; // An unsigned 16 bit integer specifying the number of resource records in records section

    //read the header from an input stream
    public static DNSHeader decodeHeader(ByteArrayInputStream in) throws IOException {
        DNSHeader header = new DNSHeader();
        header.id = in.readNBytes(2);
        byte[] headerByte2 = in.readNBytes(2);

        header.qr = (byte) (headerByte2[0] & 0xff >> 7);
        header.opcode = (byte) (headerByte2[0] >> 3 & 0xf);
        header.aa = (byte) ((headerByte2[0] << 5 & 0xff) >> 7);
        header.tc = (byte) ((headerByte2[0] << 6 & 0xff) >> 7);
        header.rd = (byte) ((headerByte2[0] << 7 & 0xff) >> 7);

        header.ra = (byte) (headerByte2[1] & 0xff >> 7);
        header.z = (byte) (((headerByte2[1] << 3 & 0xff) >> 7) & 0xf);
        header.rcode = (byte) (headerByte2[1] & 0xf);
        header.qdCount = new BigInteger(in.readNBytes(2)).shortValue();
        header.anCount = new BigInteger(in.readNBytes(2)).shortValue();
        header.nsCount = new BigInteger(in.readNBytes(2)).shortValue();
        header.arCount = new BigInteger(in.readNBytes(2)).shortValue();

        return header;
    }
    //create the header for the response. It will copy some fields from the request.
    public static DNSHeader buildResponseHeader(DNSMessage request, DNSMessage response){
        DNSHeader rHeader = new DNSHeader();
        //response.header = request.header;
        rHeader.id = request.header.id;
        rHeader.qr = 1;
        rHeader.opcode = 0;
        rHeader.aa = 0;
        rHeader.tc = 0;
        rHeader.rd = 1;
        rHeader.ra = 1;
        rHeader.z = 0;
        rHeader.rcode = 0;
        rHeader.qdCount = (short) response.questions.length;
        rHeader.anCount = (short) response.answers.length;
        rHeader.nsCount = (short) response.authorityRecords.length;
        rHeader.arCount = (short) response.resourceRecords.length;

        return rHeader;
    }
    //encode the header to bytes to be sent back to the client
    public void writeBytes(ByteArrayOutputStream out) throws IOException {
        //third byte = binary 10000001 /decimal 129
        //forth byte = binary 10000000 /decimal 128
        byte byte3 = (byte) 129;
        byte byte4 = (byte) 128;
        out.write(id);
        out.write(byte3);
        out.write(byte4);
        out.write(DNSMessage.shortToByteArr(qdCount));
        out.write(DNSMessage.shortToByteArr(anCount));
        out.write(DNSMessage.shortToByteArr(nsCount));
        out.write(DNSMessage.shortToByteArr(arCount));

    }

    @Override
    public String toString(){
        return "DNSHeader {id=" + id + ", qr=" + qr + ", opcode=" + opcode + ", aa=" + aa +
                ", tc=" + tc + ", rd=" + rd + ", ra=" + ra + ", z=" + z + ", rcode=" + rcode +
                ", qdCount=" + qdCount + ", anCount=" + anCount + ", nsCount=" + nsCount + ", arCount=" + arCount + "}";


    }

}
