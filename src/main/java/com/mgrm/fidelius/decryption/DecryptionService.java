package com.mgrm.fidelius.decryption;

import com.mgrm.fidelius.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Service
public class DecryptionService {
	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DecryptionResponse decrypt(DecryptionRequest decryptionRequest) throws Exception {
		byte[] xorOfNonces = Utils.calculateXorOfBytes(
			Utils.decodeBase64ToBytes(decryptionRequest.getSenderNonce()),
			Utils.decodeBase64ToBytes(decryptionRequest.getRequesterNonce())
		);
		byte[] iv = Arrays.copyOfRange(
			xorOfNonces,
			xorOfNonces.length - 12,
			xorOfNonces.length
		);
		byte[] salt = Arrays.copyOfRange(xorOfNonces, 0, 20);
		String decryptedData = decrypt(
			iv,
			salt,
			decryptionRequest.getRequesterPrivateKey(),
			decryptionRequest.getSenderPublicKey(),
			decryptionRequest.getEncryptedData()
		);
		return new DecryptionResponse(decryptedData);
	}

	private String decrypt(
		byte[] iv,
		byte[] salt,
		String requesterPrivateKey,
		String senderPublicKey,
		String encryptedDataAsBase64Str
	) throws Exception {
		String sharedSecret = Utils.computeSharedSecret(
			requesterPrivateKey,
			senderPublicKey
		);

		byte[] aesEncryptionKey = Utils.sha256Hkdf(salt, sharedSecret, 32);

		String decryptedData = "";
		try {
			byte[] encryptedBytes = Utils.decodeBase64ToBytes(
				encryptedDataAsBase64Str
			);

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(aesEncryptionKey, "AES");
			GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
			byte[] plainBytes = cipher.doFinal(encryptedBytes);

			decryptedData = new String(plainBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return decryptedData;
	}
}
