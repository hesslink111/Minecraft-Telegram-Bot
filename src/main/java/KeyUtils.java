import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import org.bouncycastle.jce.ECNamedCurveTable;

import javax.crypto.SecretKey;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Base64;

public class KeyUtils {
  public static KeyPair generateKeyPair() {
    try {
      var keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
      keyPairGenerator.initialize(ECNamedCurveTable.getParameterSpec("secp384r1"));
      return keyPairGenerator.generateKeyPair();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static SecretKey getEncryptionKey(String jwtString, KeyPair keyPair) {
    try {
      SignedJWT saltJwt = SignedJWT.parse(jwtString);
      URI x5u = saltJwt.getHeader().getX509CertURL();
      ECPublicKey serverKey = EncryptionUtils.generateKey(x5u.toASCIIString());
      return EncryptionUtils.getSecretKey(
          keyPair.getPrivate(),
          serverKey,
          Base64.getDecoder().decode(saltJwt.getJWTClaimsSet().getStringClaim("salt")));
    } catch (ParseException
        | NoSuchAlgorithmException
        | InvalidKeySpecException
        | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] pemKey(KeyPair keyPair) {
    var keyString =
        "-----BEGIN PRIVATE KEY-----\n"
            + new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()))
            + "\n-----END PRIVATE KEY-----\n";
    return keyString.getBytes();
  }
}
