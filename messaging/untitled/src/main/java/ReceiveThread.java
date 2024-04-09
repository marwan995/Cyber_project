import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.Key;

public class ReceiveThread implements Runnable {
    private Socket socket;
    private Key common_key;
    private String username;


    public ReceiveThread(Socket socket, Key common_key,String username) {
        this.socket = socket;
        this.common_key = common_key;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            while (true) {
                String encryptedMessage = inputStream.readUTF();
                String decryptedMessage = Utilities.decryptMessage(encryptedMessage, common_key);
                System.out.println("\n"+username+": " + decryptedMessage+" Encryption : "+encryptedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
