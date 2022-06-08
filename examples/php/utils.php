<?php

function getFideliusVersion() {
	$dirname = dirname(__FILE__);
	$gradleBuildContent = file_get_contents("$dirname/../../build.gradle");
	preg_match('/\d+\.\d+\.\d+/', $gradleBuildContent, $match);
	return $match[0];
}

function generateRandomUUID() {
	return sprintf(
		'%04x%04x-%04x-%04x-%04x-%04x%04x%04x',
		mt_rand(0, 0xffff), mt_rand(0, 0xffff),
		mt_rand(0, 0xffff),
		mt_rand(0, 0x0fff) | 0x4000,
		mt_rand(0, 0x3fff) | 0x8000,
		mt_rand(0, 0xffff), mt_rand(0, 0xffff), mt_rand(0, 0xffff)
	);
}

function ensureDirExists($filePath) {
	$dirName = dirname($filePath, 1);
	if (file_exists($dirName)) {
		return true;
	}
	ensureDirExists($dirName);
	mkdir($dirName, 0744, true);
}

?>