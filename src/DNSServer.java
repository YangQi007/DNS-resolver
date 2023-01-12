import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class DNSServer {
    DatagramSocket clientSocket;
    DatagramSocket googleSocket;
    DNSCache cache;

    public DNSServer() throws SocketException {
        clientSocket = new DatagramSocket(8053);
        googleSocket = new DatagramSocket(53);
        cache = new DNSCache();
    }

    public static void main(String[] args) throws IOException {
        DNSServer server = new DNSServer();

        while (true) {
            server.Listen();
        }
    }

    public void Listen() throws IOException {
        System.out.println("Start Listening to request...\n");

        DatagramPacket clientPacket = receivePacket(clientSocket);
        byte[] clientBytes = getBytesFromPacket(clientPacket);
        //decode the input packet
        DNSMessage clientMessage = DNSMessage.decodeMessage(clientBytes);
        ArrayList<DNSRecord> outputAnswers = new ArrayList<>();
        //look all the questions in the request
        for (DNSQuestion question : clientMessage.questions) {
            System.out.println("Requesting " + DNSMessage.octetsToString(question.qName));

            DNSRecord answer = cache.searchFor(question);
            //if valid answer, add to response
            if (answer != null) {
                outputAnswers.add(answer);
            }
            else {
                System.out.println("Domain Not In Cache!");
                //forward the request Google
                SendRequestToGoogle(clientMessage);

                System.out.println("Forward Request to Google...\n");
                DatagramPacket googlePacket = receivePacket(googleSocket);
                byte[] googleBytes = getBytesFromPacket(googlePacket);

                DNSMessage googleMessage = DNSMessage.decodeMessage(googleBytes);
                System.out.println("Add Response to Cache...\n");
                if (googleMessage.answers.length != 0) {
                    cache.addRecord(question, googleMessage.answers[0]);
                    outputAnswers.add(googleMessage.answers[0]);
                }
            }
        }

        DNSMessage response = DNSMessage.buildResponse(clientMessage, outputAnswers.toArray(new DNSRecord[outputAnswers.size()]));
        byte outputBytes[] = response.toBytes();

        System.out.println("Send Response to Client...");
        //send response back to client
        SendResponseToClient(outputBytes, clientPacket);
        System.out.println();
    }

    private DatagramPacket receivePacket(DatagramSocket Socket) throws IOException {
        byte[] Buffer = new byte[1024];
        DatagramPacket inputPacket = new DatagramPacket(Buffer, Buffer.length);
        Socket.receive(inputPacket);
        return inputPacket;
    }

    private byte[] getBytesFromPacket(DatagramPacket inputPacket) {
        byte[] outputBytes = new byte[inputPacket.getLength()];
        for (int i = 0; i < outputBytes.length; i++) {
            outputBytes[i] = inputPacket.getData()[i];
        }
        return outputBytes;
    }

    private void SendRequestToGoogle(DNSMessage message) throws IOException {
        InetAddress googleIP = InetAddress.getByName("8.8.8.8");
        DatagramPacket outputpacket = new DatagramPacket(message.byteMessage, message.byteMessage.length, googleIP, 53);
        googleSocket.send(outputpacket);
    }

    private void SendResponseToClient(byte[] outputBytes, DatagramPacket inputPacket) throws IOException {
        DatagramPacket outpacket = new DatagramPacket(outputBytes, outputBytes.length, inputPacket.getAddress(), inputPacket.getPort());
        clientSocket.send(outpacket);
    }

}
