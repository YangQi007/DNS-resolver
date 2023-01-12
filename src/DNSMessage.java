import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNSMessage {
    public DNSHeader header;
    public byte [] byteMessage;
    public DNSQuestion [] questions;
    public DNSRecord [] answers;
    public DNSRecord [] authorityRecords;
    public DNSRecord [] resourceRecords;

    public static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        DNSMessage message = new DNSMessage();
        message.byteMessage = bytes;
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);

        message.header = DNSHeader.decodeHeader(byteStream);

        message.questions = new DNSQuestion[message.header.qdCount];
        for (int i = 0; i < message.questions.length; i++) {
            message.questions[i] = DNSQuestion.decodeQuestion(byteStream, message);
        }

        message.answers = new DNSRecord[message.header.anCount];
        for (int i = 0; i < message.answers.length; i++) {
            message.answers[i] = DNSRecord.decodeRecord(byteStream, message);
        }

        message.authorityRecords = new DNSRecord[message.header.nsCount];
        for (int i = 0; i < message.authorityRecords.length; i++) {
            message.authorityRecords[i] = DNSRecord.decodeRecord(byteStream, message);
        }

        message.resourceRecords = new DNSRecord[message.header.arCount];
        for (int i = 0; i < message.resourceRecords.length; i++) {
            message.resourceRecords[i] = DNSRecord.decodeRecord(byteStream, message);
        }

        return message;
    }

    //read the pieces of a domain name starting from the current position of the input stream
    public String[] readDomainName(ByteArrayInputStream in){
        ArrayList<String> names = new ArrayList<>();
        while (true) {
            byte nameSize = (byte) in.read();
            if (nameSize == 0) {
                break;
            }
            if (nameSize < 0) {
                int mask = 0x3F;
                nameSize &= mask;
                nameSize <<= 8;
                nameSize |= in.read();
                return readDomainName(nameSize);
            }
            String name = "";
            for (int i = 0; i < nameSize; i++) {
                name += (char) in.read();
            }
            names.add(name);
        }

        return names.toArray(new String[names.size()]);

    }
    //used when there's compression and we need to find the domain from earlier in the message.
    public String[] readDomainName(int firstByte){
        ByteArrayInputStream in = new ByteArrayInputStream(byteMessage);
        in.skip(firstByte);
        return readDomainName(in);


    }

    //build a response based on the request and the answers you intend to send back.
   public static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers){
        DNSMessage response = new DNSMessage();
        response.questions = request.questions;
        response.answers = answers;
        response.resourceRecords = request.resourceRecords;
        response.authorityRecords = request.authorityRecords;
        response.header = DNSHeader.buildResponseHeader(request, response);
        return response;

    }

    //get the bytes to put in a packet and send back
   public byte[] toBytes() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        HashMap<String, Integer> domainNameLocations = new HashMap<>();

        header.writeBytes(outStream);
        for (DNSQuestion question : questions) {
            question.writeBytes(outStream, domainNameLocations);
        }
        for (DNSRecord record : answers) {
            record.writeBytes(outStream, domainNameLocations);
        }
        for (DNSRecord record : authorityRecords) {
            record.writeBytes(outStream, domainNameLocations);
        }
        for (DNSRecord record : resourceRecords) {
            record.writeBytes(outStream, domainNameLocations);
        }

        return outStream.toByteArray();

    }

    //If this is the first time we've seen this domain name in the packet, write it using the DNS encoding
    // (each segment of the domain prefixed with its length, 0 at the end), and add it to the hash map.
    // Otherwise, write a back pointer to where the domain has been seen previously.
   public static void writeDomainName(ByteArrayOutputStream os, HashMap<String,Integer> domainLocations, String[] domainPieces){
        String domainKey = octetsToString(domainPieces);

        if (domainLocations.containsKey(domainKey)) {
            int intPointer = domainLocations.get(domainKey);
            byte secondByte = (byte) intPointer;
            intPointer >>= 8;
            byte firstByte = (byte) intPointer;
            byte mask = (byte) 0xc0;
            firstByte |= mask;

            os.write(firstByte);
            os.write(secondByte);

        } else {
            domainLocations.put(domainKey, os.size());

            for (int i = 0; i < domainPieces.length; i++) {
                os.write(domainPieces[i].length());
                for (char c : domainPieces[i].toCharArray()) {
                    os.write(c);
                }
            }
            os.write(0);
        }
    }

    //join the pieces of a domain name with dots (eg.[ "utah", "edu"] -> "utah.edu" )
   public static String octetsToString(String[] octets){
        String output = "";
        for (int i = 0; i < octets.length; i++) {
            output += octets[i];
            if (i < octets.length - 1) {
                output += '.';
            }
        }
        return output;
    }

    //helper function, put short to byteArray
    public static byte[] shortToByteArr (short s){
        byte[] b = new byte[2];
        b[0] = (byte) (s >> 8 & 0xff);
        b[1] = (byte) (s & 0xff);
        return b;
    }


    @Override
    public String toString() {
        return "DNSMessage {byteMessage=" + Arrays.toString(byteMessage) + ", header=" + header + ", questions="
                + Arrays.toString(questions) + ", answers=" + Arrays.toString(answers) + ", authorityRecords="
                + Arrays.toString(authorityRecords) + ", additionalRecords=" + Arrays.toString(resourceRecords)
                + ", domainNameLocations=" + "}";
    }


}
