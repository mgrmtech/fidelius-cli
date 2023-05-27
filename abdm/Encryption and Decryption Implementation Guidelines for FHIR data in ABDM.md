# Implementation Guidelines for Encrypting and Decrypting FHIR Data in ABDM

This guide outlines the technical foundations and protocols underpinning the process of encrypting and decrypting FHIR data within the ABDM ecosystem. Key actors in this system include Health Information Providers (HIPs), Health Information Users (HIUs), and Health Information Exchange Consent Managers (HIE-CMs).

## Terminology

In this guide, the following abbreviations are used:

-   **HIP**: Health Information Provider
-   **HIU**: Health Information User
-   **HIE-CM**: Health Information Exchange Consent Manager
-   **ECDH**: Elliptic-curve Diffie–Hellman Key Exchange
-   **AES-GCM**: Advanced Encryption Standard-Galois/Counter Mode
-   **P and U**: Represent the Health Information Provider and Health Information User in the system, respectively
-   **DHPK**: Elliptic-curve Diffie–Hellman Public Key
-   **DHSK**: Elliptic-curve Diffie–Hellman Secret/Private Key
-   **RAND**: Random String
-   **DHK(U,P)**: Elliptic-curve Diffie–Hellman Key / Shared Secret Key for a specific data exchange session between U and P
-   **HKDF**: Hash-based Key Derivation Function
-   **SK(U,P)**: AES-GCM Encryption/Decryption Key for a specific data exchange session between U and P

## Security and Cryptographic Mechanism

The FHIR data within this system is securely exchanged using a cryptographic mechanism ensuring perfect forward secrecy. Consequently, even if any key materials held by HIPs, HIUs, or HIE-CM servers are compromised, previously exchanged data remains secure. This mechanism employs the Elliptic-curve Diffie–Hellman Key Exchange (ECDH), and all encryption/decryption operations use the Elliptic Curve Cryptography (ECC) curve, Curve25519. This curve and the ECDH protocol are widely utilized in internet security protocols such as SSH and TLS, and in applications like WhatsApp, to establish shared secrets between remote parties.

The diagram below provides a visual summary of the FHIR data flow between the HIP and HIU within the ABDM system.

![Summary of FHIR Data Flow between HIP and HIU](./HI%20Data%20Flow%20between%20HIP%20and%20HIU%20%C2%B7%20ABDM%20%C2%B7%20Original.drawio.png)

## Data Encryption at HIP

The data flow begins with a health information data request from the HIU. The request contains the corresponding patient's consent, and HIU's public key material (HIU's public key and nonce). The HIU key material is generated in the following steps:

1. Create a set of ECDH parameters.
2. Generate a short-term ECDH key pair (DHSK(U), DHPK(U)).
3. Produce a 32-byte random value, RAND(U), also referred to as a nonce.

The consent artefact, HIU's public key, and nonce are sent to the HIE-CM as part of the data request through a digitally-signed API call, which is then forwarded to the HIP.

Upon successful consent validation, the HIP prepares the corresponding FHIR bundle and carries out these steps:

1. Generate a new ECDH public-private key pair (DHSK(P), DHPK(P)) in the same group as specified by the HIU.
2. Generate a 32-byte random value, RAND(P), or nonce.
3. Compute an ECDH shared key, DHK(U,P), using DHPK(U) and DHSK(P).
4. Derive the IV and SALT by XORing RAND(P) and RAND(U), using the first 20 bytes as the SALT for the HKDF, and the last 12 bytes as IV for the encryption process.
5. Compute a 256-bit AES-GCM encryption key for the session, SK(U,P), using the HKDF function with the ECDH shared key and the derived SALT.
6. Encrypt the data using the derived AES-GCM encryption key and the IV.

The HIP then sends its public key DHPK(P), its nonce RAND(P), and the encrypted data to the HIU.

## Data Decryption at HIU

Upon receiving the encrypted data and HIP's public key material (the public key DHPK(P), and the nonce RAND(P)), the HIU follows these steps:

1. Query the stored data and retrieve its own key material initially used for the data request.
2. Compute the ECDH shared key, DHK(U,P), using DHPK(P) and DHSK(U).
3. Derive the IV and SALT by XORing RAND(U) and RAND(P), using the first 20 bytes as the SALT for the HKDF, and the last 12 bytes as IV for the decryption process.
4. Compute a 256-bit AES-GCM decryption key for the session, SK(U,P), using the HKDF function with the ECDH shared key and the derived SALT.
5. Decrypt the data using the derived AES-GCM decryption key and the IV.

## Code Implementation

A full implementation of the above processes — Key Material Generation, Encryption, Decryption — in Java is available in [Fidelius CLI](https://github.com/mgrmtech/fidelius-cli). The specific parts are:

-   [Key Material Generation](https://github.com/mgrmtech/fidelius-cli/blob/main/src/main/java/com/mgrm/fidelius/keypairgen/KeyPairGenController.java)
-   [Encryption](https://github.com/mgrmtech/fidelius-cli/blob/main/src/main/java/com/mgrm/fidelius/encryption/EncryptionController.java)
-   [Decryption](https://github.com/mgrmtech/fidelius-cli/blob/main/src/main/java/com/mgrm/fidelius/decryption/DecryptionController.java)

## Guidelines for Public Keys

Public keys should follow the big endian byte ordering scheme (network byte order) to maintain consistency and ensure interoperability across different systems.

Bouncy Castle's cryptographic implementations generally comply with these conventions. Additionally, public keys should be uncompressed and free of any additional headers or encoding details.

Several libraries, such as OpenSSL, Bouncy Castle, NaCl, Libgcrypt, PyNaCl, and TweetNaCl, can help with this task. These libraries offer the ability to export public keys in their uncompressed form.

For uncompressed EC public keys, including those of Curve25519, their hexadecimal representation consistently starts with the '04' prefix. This '04' prefix serves as an identifier signaling that the public key is in uncompressed form. After this '04' prefix, the hexadecimal representations of the X and Y coordinates of the EC point are provided, each 32 bytes in length.
