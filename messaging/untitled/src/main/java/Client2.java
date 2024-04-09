import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Scanner;

public class Client2 {
    static Socket receive_socket;
    static Socket send_socket;

    public Client2() {
    }
    static void initializeSocket(){
        try {
            send_socket = new Socket("localhost", 6666);
            receive_socket = new Socket("localhost", 6667);
            System.out.println("Connected successfully.");
        } catch (ConnectException var3) {
            System.err.println("Error: Server is not up yet!");
            System.exit(3);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Key setupConnection() throws NoSuchAlgorithmException {
        Map<String, BigInteger> public_keys = Utilities.readPublicKeys();
        System.out.println(public_keys);
        Map<String, BigInteger> dh_keys = Utilities.keysGeneration(public_keys.get("dh_q"), public_keys.get("dh_alpha"));
        Map<String, BigInteger> elgamal_keys = Utilities.keysGeneration(public_keys.get("elgamal_q"),public_keys.get("elgamal_alpha"));

        //exchange keys
        BigInteger recipient_elgamal_public = Utilities.exchangePublicKeys(elgamal_keys.get("public"),send_socket,receive_socket);
        Map<String, BigInteger> msg_signed = Utilities.elGamalSignature(dh_keys.get("public"),public_keys.get("elgamal_q"),public_keys.get("elgamal_alpha"),elgamal_keys.get("private"));

        Utilities.sendKeySigned(msg_signed.get("s1").toString(),msg_signed.get("s2").toString(),dh_keys.get("public").toString(),send_socket);
        String[] received_keys = Utilities.receiveKeySigned(receive_socket);
        BigInteger recipient_dh_public = new BigInteger(received_keys[2]);

        if (!Utilities.verifySignature(new BigInteger(received_keys[0]), new BigInteger(received_keys[1]), recipient_dh_public,
                recipient_elgamal_public, public_keys.get("elgamal_q"), public_keys.get("elgamal_alpha"))) {
            System.out.println("Signature verification failed. The received message may have been tampered with.");
            System.exit(1);
        }

        Key common_key = Utilities.generateCommonKey(recipient_dh_public,dh_keys.get("private"),public_keys.get("dh_q"));
        return  common_key;
    }


    public static void main(String[] args) throws  NoSuchAlgorithmException {

        System.out.println("Your name: ");
        String name = (new Scanner(System.in)).nextLine();
        initializeSocket();

        Key common_key =  setupConnection();
        System.out.println(common_key);
        String recipient_name = Utilities.exchangeNames(name,send_socket,receive_socket);

        Thread sendThread = new Thread(new SendThread(send_socket, common_key,name));
        Thread receiveThread = new Thread(new ReceiveThread(receive_socket, common_key,recipient_name));

        // Start the threads
        sendThread.start();
        receiveThread.start();

    }
}
