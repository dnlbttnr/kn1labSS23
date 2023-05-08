import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.concurrent.*;

/**
 * Die "Klasse" Sender liest einen String von der Konsole und zerlegt ihn in einzelne Worte. Jedes Wort wird in ein
 * einzelnes {@link Packet} verpackt und an das Medium verschickt. Erst nach dem Erhalt eines entsprechenden
 * ACKs wird das nächste {@link Packet} verschickt. Erhält der Sender nach einem Timeout von einer Sekunde kein ACK,
 * überträgt er das {@link Packet} erneut.
 */
public class Sender {
    /**
     * Hauptmethode, erzeugt Instanz des {@link Sender} und führt {@link #send()} aus.
     * @param args Argumente, werden nicht verwendet.
     */
    public static void main(String[] args) {
        Sender sender = new Sender();
        try {
            sender.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Erzeugt neuen Socket. Liest Text von Konsole ein und zerlegt diesen. Packt einzelne Worte in {@link Packet}
     * und schickt diese an Medium. Nutzt {@link SocketTimeoutException}, um eine Sekunde auf ACK zu
     * warten und das {@link Packet} ggf. nochmals zu versenden.
     * @throws IOException Wird geworfen falls Sockets nicht erzeugt werden können.
     */
    private void send() throws IOException {
        //Text einlesen...
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput = stdIn.readLine();
        userInput += " EOT";

        //... und in Worte zerlegen
        String[] words = userInput.split(" ");
        // Füge Leerzeichen wieder ein
        for (int i = 0; i < words.length - 1 /* ignoriere EOT */; i++) words[i] += " ";

        // Socket erzeugen auf Port 9998 und Timeout auf eine Sekunde setzen
        DatagramSocket clientSocket = new DatagramSocket(9998);
        clientSocket.setSoTimeout(1000);

        int currentWord = 0;
        int seqNumber = 1;
        while (true) {
            // Erstelle Paket
            Packet packetOut = new Packet(seqNumber, seqNumber + words[currentWord].length(), false, words[currentWord].getBytes());

            // Paket serialisierien
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(packetOut);
            byte[] sndBuf = b.toByteArray();

            // Raw Datagram Paket erstellen und abschicken
            InetAddress address = InetAddress.getLocalHost();
            DatagramPacket sndPacketRaw = new DatagramPacket(sndBuf, sndBuf.length, address, 9997);
            clientSocket.send(sndPacketRaw);

            try {
                // Auf ACK warten
                // Raw Datagram Paket erhalten
                byte[] rcvBuf = new byte[256];
                DatagramPacket rcvPacketRaw = new DatagramPacket(rcvBuf, 256);
                clientSocket.receive(rcvPacketRaw);

                // Raw Datagram deserialisieren
                ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(rcvPacketRaw.getData()));
                Packet packetIn = (Packet) is.readObject();

                seqNumber = packetIn.getAckNum();
                currentWord++;

                String rcvMessage = new String(packetIn.getPayload());
                if (rcvMessage.equals("EOT")) {
                    break;
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
            	System.out.println("Receive timed out, retrying...");
            }
        }
        
        // Wenn alle Packete versendet und von der Gegenseite bestätigt sind, Programm beenden
        clientSocket.close();
        
        if(System.getProperty("os.name").equals("Linux")) {
            clientSocket.disconnect();
        }

        System.exit(0);
    }
}
