package com.mgrm.fidelius.encryption;

import com.mgrm.fidelius.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Service
public class EncryptionService {
	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public EncryptionResponse encrypt(EncryptionRequest encryptionRequest) throws Exception {
		byte[] xorOfNonces = Utils.calculateXorOfBytes(
			Utils.decodeBase64ToBytes(encryptionRequest.getSenderNonce()),
			Utils.decodeBase64ToBytes(encryptionRequest.getRequesterNonce())
		);

		byte[] iv = Arrays.copyOfRange(
			xorOfNonces,
			xorOfNonces.length - 12,
			xorOfNonces.length
		);
		byte[] salt = Arrays.copyOfRange(xorOfNonces, 0, 20);

		String encryptedData = encrypt(
			iv,
			salt,
			encryptionRequest.getSenderPrivateKey(),
			encryptionRequest.getRequesterPublicKey(),
			encryptionRequest.getStringToEncrypt()
		);

		return new EncryptionResponse(encryptedData);
	}

	private String encrypt(
		byte[] iv,
		byte[] salt,
		String senderPrivateKey,
		String requesterPublicKey,
		String stringToEncrypt
	) throws Exception {
		String sharedSecret = Utils.computeSharedSecret(
			senderPrivateKey,
			requesterPublicKey
		);

		byte[] aesEncryptionKey = Utils.sha256Hkdf(salt, sharedSecret, 32);

		String encryptedData = "";
		try {
			byte[] stringBytes = stringToEncrypt.getBytes();

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(aesEncryptionKey, "AES");
			GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
			byte[] cipherBytes = cipher.doFinal(stringBytes);

			encryptedData = Utils.encodeBytesToBase64(cipherBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return encryptedData;
	}
}
