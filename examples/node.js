const { execSync } = require("child_process");
const path = require("path");

const binPath = path.join(__dirname, "fidelius-1.0.0", "bin", "fidelius");

const execFideliusCli = (args) => {
	const shellCommand = process.platform !== "win32" ? "sh" : "";
	const execOptions = { encoding: "utf-8" };
	const fideliusCommand = `${shellCommand} ${binPath} ${args.join(" ")}`;

	const result = execSync(fideliusCommand, execOptions);
	try {
		return JSON.parse(result.replace(/(\r\n|\n|\r)/gm, ""));
	} catch (error) {
		console.error(
			`ERROR · execFideliusCli · Command: ${args.join(" ")} ·`,
			{ result },
			error
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
		`'${stringToEncrypt}'`,
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

const runExample = () => {
	const stringToEncrypt = "There is no war in Ba Sing Se!";

	const requesterKeyMaterial = getEcdhKeyMaterial();
	const senderKeyMaterial = getEcdhKeyMaterial();

	console.log({ requesterKeyMaterial, senderKeyMaterial });

	const { encryptedData } = encryptData({
		stringToEncrypt,
		senderNonce: senderKeyMaterial.nonce,
		requesterNonce: requesterKeyMaterial.nonce,
		senderPrivateKey: senderKeyMaterial.privateKey,
		requesterPublicKey: requesterKeyMaterial.publicKey,
	});

	const { encryptedData: encryptedDataWithX509PublicKey } = encryptData({
		stringToEncrypt,
		senderNonce: senderKeyMaterial.nonce,
		requesterNonce: requesterKeyMaterial.nonce,
		senderPrivateKey: senderKeyMaterial.privateKey,
		requesterPublicKey: requesterKeyMaterial.x509PublicKey,
	});

	console.log({ encryptedData, encryptedDataWithX509PublicKey });

	const { decryptedData } = decryptData({
		encryptedData,
		requesterNonce: requesterKeyMaterial.nonce,
		senderNonce: senderKeyMaterial.nonce,
		requesterPrivateKey: requesterKeyMaterial.privateKey,
		senderPublicKey: senderKeyMaterial.publicKey,
	});

	const { decryptedData: decryptedDataWithX509PublicKey } = decryptData({
		encryptedData,
		requesterNonce: requesterKeyMaterial.nonce,
		senderNonce: senderKeyMaterial.nonce,
		requesterPrivateKey: requesterKeyMaterial.privateKey,
		senderPublicKey: senderKeyMaterial.x509PublicKey,
	});

	console.log({ decryptedData, decryptedDataWithX509PublicKey });
};

runExample();
