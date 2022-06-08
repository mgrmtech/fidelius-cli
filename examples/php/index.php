<?php

require __DIR__ . '/utils.php';

$fideliusVersion = getFideliusVersion();
$binPath = __DIR__ . "/../fidelius-cli-$fideliusVersion/bin/fidelius-cli"; 

function execFideliusCli($args) {
	$argsStr = join(" ", $args);
	$baseCommand = $GLOBALS['binPath'];
	$fideliusCommand = "$baseCommand $argsStr";
	$result = shell_exec($fideliusCommand);
	$jsonObj = json_decode($result);
	if ($jsonObj === null && json_last_error() !== JSON_ERROR_NONE) {
		echo "ERROR · execFideliusCli · Command: $argsStr\n$result";
		return;
	}
	return $jsonObj;
}

function getEcdhKeyMaterial() {
	$result = execFideliusCli(["gkm"]);
	return $result;
}

function writeParamsToFile(...$params) {
	$fileContents = join("\n", $params);
	$filePath = $GLOBALS['dirname'] . "/temp/" . generateRandomUUID() . "txt";
	ensureDirExists($filePath);
	$fileHandle = fopen($filePath, "w");
	fwrite($fileHandle, $fileContents);
	fclose($fileHandle);
	return $filePath;
}

function removeFileAtPath($filePath) {
	unlink($filePath);
}

function encryptData($encryptParams){
	$paramsFilePath = writeParamsToFile(
		"e",
		$encryptParams['stringToEncrypt'],
		$encryptParams['senderNonce'],
		$encryptParams['requesterNonce'],
		$encryptParams['senderPrivateKey'],
		$encryptParams['requesterPublicKey']
	);
	$result = execFideliusCli(["-f", $paramsFilePath]);
	removeFileAtPath($paramsFilePath);
	return $result;
}

function decryptData($decryptParams) {
	$paramsFilePath = writeParamsToFile(
		"d",
		$decryptParams['encryptedData'],
		$decryptParams['requesterNonce'],
		$decryptParams['senderNonce'],
		$decryptParams['requesterPrivateKey'],
		$decryptParams['senderPublicKey']
	);
	$result = execFideliusCli(["-f", $paramsFilePath]);
	removeFileAtPath($paramsFilePath);
	return $result;
}

function runExample($stringToEncrypt) {
	$requesterKeyMaterial = (array) getEcdhKeyMaterial();
	$senderKeyMaterial = (array) getEcdhKeyMaterial();

	echo json_encode([
		'requesterKeyMaterial' => $requesterKeyMaterial, 
		"senderKeyMaterial" => $senderKeyMaterial
	], JSON_PRETTY_PRINT) . PHP_EOL;

	$encryptionResult = (array) encryptData([
		'stringToEncrypt' => $stringToEncrypt,
		'senderNonce' => $senderKeyMaterial['nonce'],
		'requesterNonce' => $requesterKeyMaterial['nonce'],
		'senderPrivateKey' => $senderKeyMaterial['privateKey'],
		'requesterPublicKey' => $requesterKeyMaterial['publicKey'],
	]);
 
	$encryptionWithX509PublicKeyResult = (array) encryptData([
		'stringToEncrypt' => $stringToEncrypt,
		'senderNonce' => $senderKeyMaterial['nonce'],
		'requesterNonce' => $requesterKeyMaterial['nonce'],
		'senderPrivateKey' => $senderKeyMaterial['privateKey'],
		'requesterPublicKey' => $requesterKeyMaterial['x509PublicKey'],
	]);

	echo json_encode([
		'encryptedData' => $encryptionResult['encryptedData'], 
		'encryptedDataWithX509PublicKey' => $encryptionWithX509PublicKeyResult['encryptedData']
	], JSON_PRETTY_PRINT) . PHP_EOL;

	$decryptionResult = (array) decryptData([
		'encryptedData' => $encryptionResult['encryptedData'],
		'requesterNonce' => $requesterKeyMaterial['nonce'],
		'senderNonce' => $senderKeyMaterial['nonce'],
		'requesterPrivateKey' => $requesterKeyMaterial['privateKey'],
		'senderPublicKey' => $senderKeyMaterial['publicKey'],
	]);

	$decryptionResultWithX509PublicKey = (array) decryptData([
		'encryptedData' => $encryptionResult['encryptedData'],
		'requesterNonce' => $requesterKeyMaterial['nonce'],
		'senderNonce' => $senderKeyMaterial['nonce'],
		'requesterPrivateKey' => $requesterKeyMaterial['privateKey'],
		'senderPublicKey' => $senderKeyMaterial['x509PublicKey'],
	]);

	echo json_encode([
		'decryptedData' => $decryptionResult['decryptedData'], 
		'decryptedDataWithX509PublicKey' => $decryptionResultWithX509PublicKey['decryptedData']
	], JSON_PRETTY_PRINT) . PHP_EOL;
}

runExample('{"data": "There is no war in Ba Sing Se!"}');
?>
