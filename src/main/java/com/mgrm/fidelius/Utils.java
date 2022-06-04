package com.mgrm.fidelius;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.KeyAgreement;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.util.encoders.Base64;

public class Utils {

	public static String encodeBytesToBase64(byte[] value) {
		return new String(Base64.encode(value));
	}

	public static byte[] decodeBase64ToBytes(String value) {
		return Base64.decode(value);
	}

	public static String decodeBase64ToString(String value) {
		return new String(Base64.decode(value));
	}

	public static byte[] calculateXorOfBytes(
		byte[] byteArrayA,
		byte[] byteArrayB
	) {
		byte[] xorOfBytes = new byte[byteArrayA.length];
		for (int i = 0; i < byteArrayA.length; i++) {
			xorOfBytes[i] =
				(byte) (byteArrayA[i] ^ byteArrayB[i % byteArrayB.length]);
		}
		return xorOfBytes;
	}

	// A SHA-256 HKDF for generating an AES encryption key
	public static byte[] sha256Hkdf(
		byte[] salt,
		String initialKeyMaterial,
		Integer keyLengthInBytes
	) {
		HKDFBytesGenerator hkdfBytesGenerator = new HKDFBytesGenerator(
			new SHA256Digest()
		);
		HKDFParameters hkdfParameters = new HKDFParameters(
			decodeBase64ToBytes(initialKeyMaterial),
			salt,
			null
		);
		hkdfBytesGenerator.init(hkdfParameters);
		byte[] encryptionKey = new byte[keyLengthInBytes];
		hkdfBytesGenerator.generateBytes(encryptionKey, 0, keyLengthInBytes);
		return encryptionKey;
	}

	private static PrivateKey generateECPrivateKeyFromBase64Str(
		String base64PrivateKey
	)
		throws Exception {
		byte[] privateKeyBytes = decodeBase64ToBytes(base64PrivateKey);

		X9ECParameters ecParams = CustomNamedCurves.getByName(Constants.CURVE);
		ECParameterSpec ecParamSpec = new ECParameterSpec(
			ecParams.getCurve(),
			ecParams.getG(),
			ecParams.getN(),
			ecParams.getH(),
			ecParams.getSeed()
		);
		ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(
			new BigInteger(privateKeyBytes),
			ecParamSpec
		);
		return KeyFactory
			.getInstance(Constants.ALGORITHM, Constants.PROVIDER)
			.generatePrivate(privateKeySpec);
	}

	private static PublicKey generateECPublicKeyFromBase64Str(
		String base64PublicKey
	)
		throws Exception {
		byte[] publicKeyBytes = decodeBase64ToBytes(base64PublicKey);

		X9ECParameters ecParams = CustomNamedCurves.getByName(Constants.CURVE);
		ECParameterSpec ecParamSpec = new ECParameterSpec(
			ecParams.getCurve(),
			ecParams.getG(),
			ecParams.getN(),
			ecParams.getH(),
			ecParams.getSeed()
		);

		ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(
			ecParamSpec.getCurve().decodePoint(publicKeyBytes),
			ecParamSpec
		);

		return KeyFactory
			.getInstance(Constants.ALGORITHM, Constants.PROVIDER)
			.generatePublic(publicKeySpec);
	}

	private static PublicKey generateX509PublicKeyFromBase64Str(
		String base64PublicKey
	)
		throws Exception {
		byte[] publicKeyBytes = decodeBase64ToBytes(base64PublicKey);

		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
			publicKeyBytes
		);
		return KeyFactory
			.getInstance(Constants.ALGORITHM, Constants.PROVIDER)
			.generatePublic(x509EncodedKeySpec);
	}

	public static String computeSharedSecret(
		String base64PrivateKey,
		String base64PublicKey
	)
		throws Exception {
		PrivateKey privateKey = generateECPrivateKeyFromBase64Str(
			base64PrivateKey
		);
		// X509 encoded base64 public key string has 412 characters
		PublicKey publicKey = base64PublicKey.length() == 88
			? generateECPublicKeyFromBase64Str(base64PublicKey)
			: generateX509PublicKeyFromBase64Str(base64PublicKey);

		KeyAgreement keyAgreement = KeyAgreement.getInstance(
			Constants.ALGORITHM,
			Constants.PROVIDER
		);
		keyAgreement.init(privateKey);
		keyAgreement.doPhase(publicKey, true);
		byte[] sharedSecretBytes = keyAgreement.generateSecret();
		return Utils.encodeBytesToBase64(sharedSecretBytes);
	}

	public static String[] readArgsFromFile(String filepath) {
		ArrayList<String> argsFromFile = new ArrayList<String>();

		File file = new File(filepath);
		Scanner readFromFile = null;
		String line = null;

		try {
			readFromFile = new Scanner(file);
		} catch (FileNotFoundException exception) {
			System.out.println(
				"ERROR · File not found at the given filepath · " + filepath
			);
		}
		while (readFromFile.hasNextLine()) {
			line = readFromFile.nextLine();
			argsFromFile.add(line);
		}
		String[] returnValue = argsFromFile.toArray(
			new String[argsFromFile.size()]
		);

		return returnValue;
	}

	public static Boolean validateCliArguments(String[] args) {
		Boolean unsupportedCommand =
			args.length == 0 ||
			(
				args.length >= 1 &&
				!(
					args[0].equals("gkm") ||
					args[0].equals("generate-key-material") ||
					args[0].equals("e") ||
					args[0].equals("encrypt") ||
					args[0].equals("se") ||
					args[0].equals("sane-encrypt") ||
					args[0].equals("d") ||
					args[0].equals("decrypt")
				)
			);
		Boolean fideliusHelpNeeded =
			args.length >= 1 && (args[0].equals("help") || args[0].equals("h"));
		Boolean badEncryptionParams =
			args.length >= 1 &&
			(args[0].equals("e") || args[0].equals("encrypt")) &&
			args.length < 6;
		Boolean badDecryptionParams =
			args.length >= 1 &&
			(args[0].equals("d") || args[0].equals("decrypt")) &&
			args.length < 6;

		String gkmHelp = new String(
			"\nThe generate-key-material (or gkm) command generates an ECDH key pair, and a random nonce.\n"
		);
		String encryptionHelp = new String(
			"\nThe encrypt (or e) command takes the following additional arguments" +
			(
				badEncryptionParams && args.length > 1
					? (
						", while only " +
						(args.length - 1) +
						" additional arguments were given"
					)
					: ""
			) +
			".\n" +
			"<string-to-encrypt> <sender-nonce> <requester-nonce> <sender-private-key> <requester-public-key>\n"
		);

		String decryptionHelp = new String(
			"\nThe decrypt (or d) command takes the following additional arguments" +
			(
				badDecryptionParams && args.length > 1
					? (
						", while only " +
						(args.length - 1) +
						" additional argument(s) were given"
					)
					: ""
			) +
			".\n" +
			"<encrypted-data> <requester-nonce> <sender-nonce> <requester-private-key> <sender-public-key>\n"
		);

		String filepathFlagHelp = new String(
			"\nThe --filepath (or -f) flag can be used to provide the CLI its parameters (command and the subsequent arguments) from a text file.\n"
		);

		String unsupportedCommandHelp = new String(
			"\nFidelius CLI " +
			(args.length == 0 || fideliusHelpNeeded ? "" : "only ") +
			"accepts one of these commands/flags: generate-key-material (or gkm), encrypt (or e), sane-encrypt (or se), decrypt (or d), and --filepath (or -f).\n" +
			gkmHelp +
			encryptionHelp +
			decryptionHelp +
			filepathFlagHelp
		);

		String fellowWithTheMehBag = new String(
			(unsupportedCommand ? unsupportedCommandHelp : "") +
			(badEncryptionParams ? encryptionHelp : "") +
			(badDecryptionParams ? decryptionHelp : "") +
			"\nFor more details on Fidelius CLI, please refer to the README at https://github.com/mgrmtech/fidelius-cli\n" +
			"     ___         \n" +
			"    |   |        \n" +
			"    |   |        \n" +
			"   ,;;;;;,       \n" +
			"    /   |        \n" +
			"   |:-OO|_       \n" +
			"   C  '' _)      \n" +
			"    \\  _|       \n" +
			"     ) /         \n" +
			"   /`\\          \n" +
			"   || |Y|        \n" +
			"   || |#|        \n" +
			"   || |#|        \n" +
			"   || |#|        \n" +
			"   :| |=:        \n" +
			"   ||_|,|        \n" +
			"   \\)))||       \n" +
			"|~~~~`-`~~~~|    \n" +
			"|System.out.|    \n" +
			"|println(\"wh|   \n" +
			"|y so meh!\")|   \n" +
			"|___________|    \n" +
			"    | ||         \n" +
			"    |_||__       \n" +
			"    (____))      \n"
		);

		Boolean invalidArgs =
			fideliusHelpNeeded ||
			unsupportedCommand ||
			badEncryptionParams ||
			badDecryptionParams;
		if (invalidArgs) System.out.println(fellowWithTheMehBag);

		return invalidArgs;
	}
}
