import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

//Ctrl Alt 0L
public class Utilities {
    static MessageDigest SHA1,SHA256;


    static {
        try {
            SHA1 = MessageDigest.getInstance("SHA-1");
            SHA256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Utilities() throws NoSuchAlgorithmException {
    }

    public static Map<String, BigInteger> readPublicKeys() {
        Map<String, BigInteger> public_keys = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("Public_keys.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    public_keys.put(parts[0].trim(), new BigInteger(parts[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return public_keys;

    }

    public static Map<String, BigInteger> keysGeneration(BigInteger q, BigInteger alpha) {
        Map<String, BigInteger> key = new HashMap<>();
        BigInteger private_key =generateRandomBigInteger(q,q);
        System.out.println(private_key);
        key.put("private", private_key);
        key.put("public", alpha.modPow(private_key, q));

        return key;
    }


    public static Map<String, BigInteger> elGamalSignature(BigInteger dh_public, BigInteger q, BigInteger alpha,BigInteger elGamal_private) throws NoSuchAlgorithmException {
        Map<String, BigInteger> msg_signed = new HashMap<>();
        byte[] hashedMessage = SHA1.digest(dh_public.toByteArray());
        BigInteger m =new BigInteger(hashedMessage);//

        BigInteger k = generateRandomBigInteger(q,q.subtract(BigInteger.ONE));//
        BigInteger s1 = alpha.modPow(k, q);
        BigInteger k_inverse = k.modInverse(q.subtract(BigInteger.ONE));
        BigInteger s2_temp = k_inverse.multiply(m.subtract(elGamal_private.multiply(s1))).mod(q.subtract(BigInteger.ONE));
        BigInteger s2 = s2_temp.mod(q.subtract(BigInteger.ONE));
        msg_signed.put("s1",s1);
        msg_signed.put("s2",s2);
        return msg_signed;


    }
    public static boolean verifySignature(BigInteger s1,BigInteger s2 ,BigInteger dh_public,BigInteger elgamal_public,BigInteger q,BigInteger alpha){
        byte[] hashedMessage = SHA1.digest(dh_public.toByteArray());
        BigInteger m = new BigInteger(hashedMessage);
        BigInteger V = (elgamal_public.modPow(s1,q)).multiply(s1.modPow(s2,q)).mod(q);
        BigInteger W = alpha.modPow(m,q);

        return V.equals(W);

    }


    public static BigInteger diffieHellmanSecretKey(BigInteger public_key, BigInteger private_key, BigInteger q) {
        BigInteger secret_key = public_key.modPow(private_key, q);
        return secret_key;
    }
    public static BigInteger generateRandomBigInteger(BigInteger max, BigInteger modulus) {
        SecureRandom random = new SecureRandom();
        BigInteger randomNumber;
        do {
            byte[] randomBytes = new byte[max.bitLength() / 8];
            random.nextBytes(randomBytes);
            randomNumber = new BigInteger(randomBytes).abs();
        } while (randomNumber.compareTo(max) >= 0 || !randomNumber.gcd(modulus).equals(BigInteger.ONE)||randomNumber.equals(0));

        return randomNumber;
    }
    static  void sendMessage (String msg, Socket socket){
        try {
            DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
            outToServer.writeUTF(msg);
            outToServer.flush();
            System.out.println("Message sent to Client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void sendKeySigned(String s1,String s2,String df_public,Socket socket){
        Utilities.sendMessage(s1,socket);
        Utilities.sendMessage(s2,socket);
        Utilities.sendMessage(df_public,socket);
    }
    static String receiveMessage(Socket socket) {
        try {
            DataInputStream inFromServer = new DataInputStream(socket.getInputStream());
            return inFromServer.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static String[] receiveKeySigned(Socket socket) {
        String[] keys = new String[3];
        keys[0] = receiveMessage(socket);
        keys[1] = receiveMessage(socket);
        keys[2] = receiveMessage(socket);
        return keys;
    }
    static BigInteger exchangePublicKeys(BigInteger key,Socket send,Socket receive){
        sendMessage(key.toString(),send);
        return new BigInteger(receiveMessage(receive));
    }
    public static byte[] encryptAES(String data, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes());
    }
    public static String decryptAES(byte[] encryptedData, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes);
    }
}
