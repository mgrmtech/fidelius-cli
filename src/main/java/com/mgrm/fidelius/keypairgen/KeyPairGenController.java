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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/keys")
public class KeyPairGenController {
	private final KeyPairGenService keyPairGenService;

	@Autowired
	public KeyPairGenController(KeyPairGenService keyPairGenService) {
		this.keyPairGenService = keyPairGenService;
	}

	@GetMapping("/generate")
	public ResponseEntity<KeyMaterial> generateKeys() throws Exception {
		KeyMaterial keyMaterial = keyPairGenService.generateKeys();
		return ResponseEntity.ok(keyMaterial);
	}

	static {
		try {
			Security.addProvider(new BouncyCastleProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
