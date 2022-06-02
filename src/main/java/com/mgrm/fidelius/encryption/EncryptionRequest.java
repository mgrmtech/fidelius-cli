package com.mgrm.fidelius.encryption;

public class EncryptionRequest {

	private String stringToEncrypt;
	private String senderNonce;
	private String requesterNonce;
	private String senderPrivateKey;
	private String requesterPublicKey;

	public EncryptionRequest(
		String stringToEncrypt,
		String senderNonce,
		String requesterNonce,
		String senderPrivateKey,
		String requesterPublicKey
	) {
		this.stringToEncrypt = stringToEncrypt;
		this.senderNonce = senderNonce;
		this.requesterNonce = requesterNonce;
		this.senderPrivateKey = senderPrivateKey;
		this.requesterPublicKey = requesterPublicKey;
	}

	public String getStringToEncrypt() {
		return stringToEncrypt;
	}

	public void setStringToEncrypt(String stringToEncrypt) {
		this.stringToEncrypt = stringToEncrypt;
	}

	public String getSenderNonce() {
		return senderNonce;
	}

	public void setSenderNonce(String senderNonce) {
		this.senderNonce = senderNonce;
	}

	public String getRequesterNonce() {
		return requesterNonce;
	}

	public void setRequesterNonce(String requesterNonce) {
		this.requesterNonce = requesterNonce;
	}

	public String getSenderPrivateKey() {
		return senderPrivateKey;
	}

	public void setSenderPrivateKey(String senderPrivateKey) {
		this.senderPrivateKey = senderPrivateKey;
	}

	public String getRequesterPublicKey() {
		return requesterPublicKey;
	}

	public void setRequesterPublicKey(String requesterPublicKey) {
		this.requesterPublicKey = requesterPublicKey;
	}
}
