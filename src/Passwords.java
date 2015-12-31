import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Passwords {

	public static byte[] getHash(String a) throws UnsupportedEncodingException, NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(a.getBytes("UTF-8"));
		byte[] hash = digest.digest();
		return hash;
	}
	
}
