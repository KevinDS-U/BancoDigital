import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class HashUtil {

    public static String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static byte[] hashPassword(String password, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes());
        return md.digest(password.getBytes("UTF-8"));
    }

    public static String hashPasswordBase64(String password, String salt) throws Exception {
        return Base64.getEncoder().encodeToString(hashPassword(password, salt));
    }
}