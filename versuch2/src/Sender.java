import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.Scanner;

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
        //Text einlesen und in Worte zerlegen
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine() + " EOT";
        String[] words = input.split(" ");

        for (int i = 0; i < words.length-1; i++) {
            words[i] += " ";
        }

        // Socket erzeugen auf Port 9998 und Timeout auf eine Sekunde setzen
        DatagramSocket socket = new DatagramSocket(9998);
        socket.setSoTimeout(1000);

        // Iteration über den Konsolentext
        int currentWord = 0;
        int seqNumber = 0;
        while (currentWord < words.length) {
            // Paket an Port 9997 senden
            Packet packetOut = new Packet(seqNumber, seqNumber+words[currentWord].length(), false, words[currentWord].getBytes());
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(packetOut);
            byte[] buf = b.toByteArray();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9997);
            socket.send(packet);

            try {
                // Auf ACK warten und erst dann Schleifenzähler inkrementieren
                byte[] ackBuf = new byte[256];
                DatagramPacket ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
                socket.receive(ackPacket);
                ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(ackPacket.getData()));
                Packet incoming = (Packet) is.readObject();
                seqNumber = incoming.getAckNum();
                currentWord++;

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                System.out.println("Receive timed out, retrying...");
            }
        }

        // Wenn alle Packete versendet und von der Gegenseite bestätigt sind, Programm beenden
        socket.close();

        if(System.getProperty("os.name").equals("Linux")) {
            socket.disconnect();
        }

        System.exit(0);
    }
}
