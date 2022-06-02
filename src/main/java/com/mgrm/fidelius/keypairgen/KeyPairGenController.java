package com.mgrm.fidelius.keypairgen;

import com.mgrm.fidelius.Constants;
import com.mgrm.fidelius.Utils;
import java.security.*;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class KeyPairGenController {
	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public KeyMaterial generate() throws Exception {
		KeyPair keyPair = generateKeyPair();
		String privateKey = getEncodedPrivateKeyAsBase64Str(
			keyPair.getPrivate()
		);
		String publicKey = getEncodedPublicKeyAsBase64Str(keyPair.getPublic());
		String x509PublicKey = getX509EncodedPublicKeyAsBase64Str(
			keyPair.getPublic()
		);
		String nonce = generateBase64Nonce();
		return new KeyMaterial(privateKey, publicKey, x509PublicKey, nonce);
	}

	private KeyPair generateKeyPair()
		throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
			Constants.ALGORITHM,
			Constants.PROVIDER
		);
		X9ECParameters ecParams = CustomNamedCurves.getByName(Constants.CURVE);
		ECParameterSpec ecSpec = new ECParameterSpec(
			ecParams.getCurve(),
			ecParams.getG(),
			ecParams.getN(),
			ecParams.getH(),
			ecParams.getSeed()
		);

		keyPairGenerator.initialize(ecSpec, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

	private String getEncodedPrivateKeyAsBase64Str(PrivateKey privateKey)
		throws Exception {
		ECPrivateKey ecPrivateKey = (ECPrivateKey) privateKey;
		return Utils.encodeBytesToBase64(ecPrivateKey.getD().toByteArray());
	}

	private String getEncodedPublicKeyAsBase64Str(PublicKey publicKey)
		throws Exception {
		ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
		return Utils.encodeBytesToBase64(ecPublicKey.getQ().getEncoded(false));
	}

	private String getX509EncodedPublicKeyAsBase64Str(PublicKey publicKey)
		throws Exception {
		ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
		return Utils.encodeBytesToBase64(ecPublicKey.getEncoded());
	}

	private String generateBase64Nonce() {
		byte[] salt = new byte[32];
		SecureRandom random = new SecureRandom();
		random.nextBytes(salt);
		return Utils.encodeBytesToBase64(salt);
	}
}
