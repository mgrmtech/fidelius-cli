package com.mgrm.fidelius.encryption;

import com.mgrm.fidelius.Utils;
import java.security.Security;
import java.util.Arrays;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class EncryptionController {
	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public EncryptionResponse encrypt(EncryptionRequest encryptionRequest)
		throws Exception {
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
	)
		throws Exception {
		String sharedSecret = Utils.computeSharedSecret(
			senderPrivateKey,
			requesterPublicKey
		);

		byte[] aesEncryptionKey = Utils.sha256Hkdf(salt, sharedSecret, 32);

		String encryptedData = "";
		try {
			byte[] stringBytes = stringToEncrypt.getBytes();

			GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
			AEADParameters parameters = new AEADParameters(
				new KeyParameter(aesEncryptionKey),
				128,
				iv,
				null
			);

			cipher.init(true, parameters);
			byte[] cipherBytes = new byte[cipher.getOutputSize(
				stringBytes.length
			)];
			int encryptedBytesLength = cipher.processBytes(
				stringBytes,
				0,
				stringBytes.length,
				cipherBytes,
				0
			);
			cipher.doFinal(cipherBytes, encryptedBytesLength);

			encryptedData = Utils.encodeBytesToBase64(cipherBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return encryptedData;
	}
}
