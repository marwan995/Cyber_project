import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.util.Scanner;

public class SendThread implements Runnable {
    private Socket socket;
    private Key common_key;
    private String username;

    public SendThread(Socket socket, Key common_key,String username) {
        this.socket = socket;
        this.common_key = common_key;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            while (true) {

                String message = scanner.nextLine();
                System.out.print("\b".repeat(message.length()));

                // Encrypt the message using commonKey and send it
                String encryptedMessage = Utilities.encryptMessage(message, common_key);
                outputStream.writeUTF(encryptedMessage);
                outputStream.flush();
                System.out.print(username+": "+message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
