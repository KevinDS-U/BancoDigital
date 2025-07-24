

public class TestHashYLogin {
    public static void main(String[] args) {
        try {
            String password = "123456";
            String salt = HashUtil.generateSalt();
            String hash = HashUtil.hashPasswordBase64(password, salt);

            System.out.println("Salt generado: " + salt);
            System.out.println("Hash codificado: " + hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}