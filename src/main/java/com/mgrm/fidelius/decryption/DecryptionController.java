package com.mgrm.fidelius.decryption;

import com.mgrm.fidelius.Utils;
import java.security.Security;
import java.util.Arrays;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class DecryptionController {
	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DecryptionResponse decrypt(DecryptionRequest decryptionRequest)
		throws Exception {
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
	)
		throws Exception {
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

			GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
			AEADParameters parameters = new AEADParameters(
				new KeyParameter(aesEncryptionKey),
				128,
				iv,
				null
			);

			cipher.init(false, parameters);
			byte[] cipherBytes = new byte[cipher.getOutputSize(
				encryptedBytes.length
			)];
			int encryptedBytesLength = cipher.processBytes(
				encryptedBytes,
				0,
				encryptedBytes.length,
				cipherBytes,
				0
			);
			cipher.doFinal(cipherBytes, encryptedBytesLength);

			decryptedData = new String(cipherBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return decryptedData;
	}
}
