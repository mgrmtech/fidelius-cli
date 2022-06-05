const path = require("path");
const { execSync } = require("child_process");
const { writeFileSync, unlinkSync } = require("fs");
const {
	getFideliusVersion,
	generateRandomUUID,
	ensureDirExists,
} = require("./utils.js");

/* NOTE
 **
 ** Run the gradle task addBuildDistToExamples ("./gradlew addBuildDistToExamples"),
 ** before running this example, to ensure that the binPath exists, and
 ** it contains the latest fidelius-cli distribution.
 */

const fideliusVersion = getFideliusVersion();
const binPath = path.join(
	__dirname,
	`../fidelius-cli-${fideliusVersion}/bin/fidelius-cli`
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

const writeParamsToFile = (...params) => {
	const fileContents = params.join("\n");
	const filepath = path.join(
		__dirname,
		"temp",
		`${generateRandomUUID()}.txt`
	);
	ensureDirExists(filepath);
	writeFileSync(filepath, fileContents);
	return filepath;
};

const removeFileAtPath = (filepath) => unlinkSync(filepath);

const encryptData = ({
	stringToEncrypt,
	senderNonce,
	requesterNonce,
	senderPrivateKey,
	requesterPublicKey,
}) => {
	const paramsFilepath = writeParamsToFile(
		"e",
		stringToEncrypt,
		senderNonce,
		requesterNonce,
		senderPrivateKey,
		requesterPublicKey
	);
	const result = execFideliusCli(["-f", paramsFilepath]);
	removeFileAtPath(paramsFilepath);
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
	const paramsFilepath = writeParamsToFile(
		"se",
		base64EncodedStringToEncrypt,
		senderNonce,
		requesterNonce,
		senderPrivateKey,
		requesterPublicKey
	);
	const result = execFideliusCli(["-f", paramsFilepath]);
	removeFileAtPath(paramsFilepath);
	return result;
};

const decryptData = ({
	encryptedData,
	requesterNonce,
	senderNonce,
	requesterPrivateKey,
	senderPublicKey,
}) => {
	const paramsFilepath = writeParamsToFile(
		"d",
		encryptedData,
		requesterNonce,
		senderNonce,
		requesterPrivateKey,
		senderPublicKey
	);
	const result = execFideliusCli(["-f", paramsFilepath]);
	removeFileAtPath(paramsFilepath);
	return result;
};

const runExample = ({ stringToEncrypt }) => {
	const requesterKeyMaterial = getEcdhKeyMaterial();
	const senderKeyMaterial = getEcdhKeyMaterial();

	console.log({ requesterKeyMaterial, senderKeyMaterial });

	const encryptionResult = encryptData({
		stringToEncrypt,
		senderNonce: senderKeyMaterial.nonce,
		requesterNonce: requesterKeyMaterial.nonce,
		senderPrivateKey: senderKeyMaterial.privateKey,
		requesterPublicKey: requesterKeyMaterial.publicKey,
	});

	const encryptionWithX509PublicKeyResult = encryptData({
		stringToEncrypt,
		senderNonce: senderKeyMaterial.nonce,
		requesterNonce: requesterKeyMaterial.nonce,
		senderPrivateKey: senderKeyMaterial.privateKey,
		requesterPublicKey: requesterKeyMaterial.x509PublicKey,
	});

	console.log({
		encryptedData: encryptionResult?.encryptedData,
		encryptedDataWithX509PublicKey:
			encryptionWithX509PublicKeyResult?.encryptedData,
	});

	const decryptionResult = decryptData({
		encryptedData: encryptionResult?.encryptedData,
		requesterNonce: requesterKeyMaterial.nonce,
		senderNonce: senderKeyMaterial.nonce,
		requesterPrivateKey: requesterKeyMaterial.privateKey,
		senderPublicKey: senderKeyMaterial.publicKey,
	});

	const decryptionResultWithX509PublicKey = decryptData({
		encryptedData: encryptionResult?.encryptedData,
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
