package com.mgrm.fidelius.keypairgen;

public class KeyMaterial {

	private String privateKey;
	private String publicKey;
	private String x509PublicKey;
	private String nonce;

	public KeyMaterial(
		String privateKey,
		String publicKey,
		String x509PublicKey,
		String nonce
	) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.x509PublicKey = x509PublicKey;
		this.nonce = nonce;
	}

	public String getX509PublicKey() {
		return x509PublicKey;
	}

	public void setX509PublicKey(String x509PublicKey) {
		this.x509PublicKey = x509PublicKey;
	}

	public String getECPrivateKey() {
		return privateKey;
	}

	public void setECPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getECPublicKey() {
		return publicKey;
	}

	public void setECPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
}
