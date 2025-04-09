package auth.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class AESProvider {

  private final String secretKeyRaw;
  private final SecretKeySpec secretKeySpec;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 16; // 16 bytes = 128 bits

    public AESProvider(@Value("${aes.secret}") String secretKeyRaw) {
      this.secretKeyRaw = secretKeyRaw;
      byte[] keyBytes = secretKeyRaw.substring(0, KEY_LENGTH).getBytes(StandardCharsets.UTF_8);
      this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
  }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[KEY_LENGTH];
            new SecureRandom().nextBytes(iv); // 랜덤 IV
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES CBC 암호화 실패", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[KEY_LENGTH];
            byte[] encrypted = new byte[decoded.length - KEY_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, KEY_LENGTH);
            System.arraycopy(decoded, KEY_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES CBC 복호화 실패", e);
        }
    }
}