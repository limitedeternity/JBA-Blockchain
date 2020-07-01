import javafx.util.Pair;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class StringUtils {
    public static String repeat(String s, int count) {
        return count > 0 ? s + repeat(s, count - 1) : "";
    }

    private static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Pair<String, String> generateRandomHash(int hashZeros) {
        SecureRandom rnd = new SecureRandom();
        String magic = Integer.toString(rnd.nextInt(Integer.MAX_VALUE));
        String hash = applySha256(magic);
        while (!hash.startsWith(repeat("0", hashZeros))) {
            magic = Integer.toString(rnd.nextInt(Integer.MAX_VALUE));
            hash = applySha256(magic);
        }

        return new Pair<>(magic, hash);
    }

    public static String generateRandomString(int length) {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }

        return sb.toString();
    }
}