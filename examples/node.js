const { execSync } = require("child_process");
const path = require("path");

const binPath = path.join(
	__dirname,
	"fidelius-cli-1.1.0",
	"bin",
	"fidelius-cli"
);

const execFideliusCli = (args) => {
	const shellCommand = process.platform !== "win32" ? "sh" : "";
	const execOptions = { encoding: "utf-8" };
	const fideliusCommand = `${shellCommand} ${binPath} ${args.join(" ")}`;

	const result = execSync(fideliusCommand, execOptions);
	try {
		return JSON.parse(result.replace(/(\r\n|\n|\r)/gm, ""));
	} catch (error) {
		console.error(
			`ERROR · execFideliusCli · Command: ${args.join(" ")}\n${result}`
		);
	}
};

const getEcdhKeyMaterial = () => {
	const result = execFideliusCli(["gkm"]);
	return result;
};

const encryptData = ({
	stringToEncrypt,
	senderNonce,
	requesterNonce,
	senderPrivateKey,
	requesterPublicKey,
}) => {
	const result = execFideliusCli([
		"e",
		stringToEncrypt,
		senderNonce,
		requesterNonce,
		senderPrivateKey,
		requesterPublicKey,
	]);
	return result;
};

const saneEncryptData = ({
	stringToEncrypt,
	senderNonce,
	requesterNonce,
	senderPrivateKey,
	requesterPublicKey,
}) => {
	const base64EncodedStringToEncrypt =
		Buffer.from(stringToEncrypt).toString("base64");
	const result = execFideliusCli([
		"se",
		base64EncodedStringToEncrypt,
		senderNonce,
		requesterNonce,
		senderPrivateKey,
		requesterPublicKey,
	]);
	return result;
};

const decryptData = ({
	encryptedData,
	requesterNonce,
	senderNonce,
	requesterPrivateKey,
	senderPublicKey,
}) => {
	const result = execFideliusCli([
		"d",
		encryptedData,
		requesterNonce,
		senderNonce,
		requesterPrivateKey,
		senderPublicKey,
	]);
	return result;
};

const runExample = ({ stringToEncrypt }) => {
	const requesterKeyMaterial = getEcdhKeyMaterial();
	const senderKeyMaterial = getEcdhKeyMaterial();

	console.log({ requesterKeyMaterial, senderKeyMaterial });

	const saneEncryptionResult = saneEncryptData({
		stringToEncrypt,
		senderNonce: senderKeyMaterial.nonce,
		requesterNonce: requesterKeyMaterial.nonce,
		senderPrivateKey: senderKeyMaterial.privateKey,
		requesterPublicKey: requesterKeyMaterial.publicKey,
	});

	const saneEncryptionWithX509PublicKeyResult = saneEncryptData({
		stringToEncrypt,
		senderNonce: senderKeyMaterial.nonce,
		requesterNonce: requesterKeyMaterial.nonce,
		senderPrivateKey: senderKeyMaterial.privateKey,
		requesterPublicKey: requesterKeyMaterial.x509PublicKey,
	});

	console.log({
		encryptedData: saneEncryptionResult?.encryptedData,
		encryptedDataWithX509PublicKey:
			saneEncryptionWithX509PublicKeyResult?.encryptedData,
	});

	const decryptionResult = decryptData({
		encryptedData: saneEncryptionResult?.encryptedData,
		requesterNonce: requesterKeyMaterial.nonce,
		senderNonce: senderKeyMaterial.nonce,
		requesterPrivateKey: requesterKeyMaterial.privateKey,
		senderPublicKey: senderKeyMaterial.publicKey,
	});

	const decryptionResultWithX509PublicKey = decryptData({
		encryptedData: saneEncryptionResult?.encryptedData,
		requesterNonce: requesterKeyMaterial.nonce,
		senderNonce: senderKeyMaterial.nonce,
		requesterPrivateKey: requesterKeyMaterial.privateKey,
		senderPublicKey: senderKeyMaterial.x509PublicKey,
	});

	console.log({
		decryptedData: decryptionResult?.decryptedData,
		decryptedDataWithX509PublicKey:
			decryptionResultWithX509PublicKey?.decryptedData,
	});
};

runExample({ stringToEncrypt: '{"data": "There is no war in Ba Sing Se!"}' });
