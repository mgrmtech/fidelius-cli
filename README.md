# Fidelius CLI

Fidelius CLI is a specialized command-line interface (CLI) tool designed for ECDH cryptography. It is based on Curve25519 and incorporates a custom HMAC-based Key Derivation Function, enabling the generation of AES-GCM keys for data encryption and decryption.

While Fidelius CLI can be used for various end-to-end encryption needs, its primary purpose is to secure health data within the ABDM ecosystem. ABDM stands for Ayushman Bharat Digital Mission, an initiative by the Indian Government to establish a nationwide digital infrastructure supporting integrated digital health services.

The diagram below provides a visual representation of the health data flow between a Health Information User and a Health Information Provider, who play vital roles in the ABDM system. The encryption and decryption steps in the flowchart are appropriately labeled as "HANDLED BY FIDELIUS CLI" to indicate the involvement of Fidelius CLI in these processes.

![Summary of FHIR Data Flow between HIP and HIU, with Fidelius CLI](./abdm/HI%20Data%20Flow%20between%20HIP%20and%20HIU%20%C2%B7%20ABDM%20%C2%B7%20FIDELIUS%20CLI.drawio.png)

For more detailed information on the technical foundations and protocols involved in encrypting and decrypting FHIR data within the ABDM ecosystem, you can refer to [this comprehensive guide](./abdm/Encryption%20and%20Decryption%20Implementation%20Guidelines%20for%20FHIR%20data%20in%20ABDM.md).

## Build

-   The following command(s) would build and generate .zip, and .tar distributions inside ./build/distributions

```
# BASH shell
./gradlew clean build jar

# BASH shell in Windows (e.g. GIT Bash)
TERM=cygwin ./gradlew clean build jar

# Windows CMD shell
gradlew clean build jar
```

## Usage

-   A pre-built release can be downloaded from [here](https://github.com/mgrmtech/fidelius-cli/releases). Please ensure that JRE 1.8+ is installed, to run the binaries in the release.

-   Fidelius CLI works with the following commands

    -   `generate-key-material` (or `gkm`) · The `generate-key-material` command generates an ECDH key pair, and a random nonce.

    ```bash
    $ ./fidelius-cli gkm
    ```

    -   `encrypt` (or `e`) · The `encrypt` command takes the following additional arguments.

    ```bash
    $ ./fidelius-cli e\
      <string-to-encrypt>\
      <sender-nonce>\
      <requester-nonce>\
      <sender-private-key>\
      <requester-public-key>
    ```

    -   `sane-encrypt` (or `se`) · The `sane-encrypt` command behaves identically to the encrypt command, with the only difference being that it accepts base64 encoded version of the input string. Fidelius would decode this base64 value to the original string, before encrypting it. This is available to circumvent the need to escape special characters in strings (e.g. JSON values).

    ```bash
    $ ./fidelius-cli se\
      <string-to-encrypt-base64-encoded>\
      <sender-nonce>\
      <requester-nonce>\
      <sender-private-key>\
      <requester-public-key>
    ```

    -   `decrypt` (or `d`) · The `decrypt` command takes the following additional arguments.

    ```bash
    $ ./fidelius-cli d\
      <encrypted-data>\
      <requester-nonce>\
      <sender-nonce>\
      <requester-private-key>\
      <sender-public-key>\;
    ```

    -   `--filepath` (or `-f`) · The `--filepath` flag can be used to provide the CLI its parameters (command and the subsequent arguments) from a text file. This can be used as a workaround to the Windows' terminals' ["This command is too long" (>8192 characters) limitation](https://docs.microsoft.com/en-us/troubleshoot/windows-client/shell-experience/command-line-string-limitation) in case of long input strings.

    ```bash
    $ ./fidelius-cli -f /path/to/params/file.txt
    ```

-   The following set of commands demonstrate an example for the usage of the above commands.

### Key Material Generation

```bash
$ cd fidelius-cli-1.x.x/bin
$ ./fidelius-cli gkm
{
  "privateKey": "DMxHPri8d7IT23KgLk281zZenMfVHSdeamq0RhwlIBk=",
  "publicKey": "BAheD5rUqTy4V5xR4/6HWmYpopu5CO+KO8BECS0udNqUTSNo91TIqIIy1A4Vh+F94c+n9vAcwXU2bGcfsI5f69Y=",
  "x509PublicKey": "MIIBMTCB6gYHKoZIzj0CATCB3gIBATArBgcqhkjOPQEBAiB/////////////////////////////////////////7TBEBCAqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqYSRShRAQge0Je0Je0Je0Je0Je0Je0Je0Je0Je0Je0JgtenHcQyGQEQQQqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq0kWiCuGaG4oIa04B7dLHdI0UySPU1+bXxhsinpxaJ+ztPZAiAQAAAAAAAAAAAAAAAAAAAAFN753qL3nNZYEmMaXPXT7QIBCANCAAQIXg+a1Kk8uFecUeP+h1pmKaKbuQjvijvARAktLnTalE0jaPdUyKiCMtQOFYfhfeHPp/bwHMF1NmxnH7COX+vW",
  "nonce": "6uj1RdDUbcpI3lVMZvijkMC8Te20O4Bcyz0SyivX8Eg="
}
```

Let's suppose the above output represents the generated key material of the requester.

```bash
$ ./fidelius-cli gkm
{
  "privateKey": "AYhVZpbVeX4KS5Qm/W0+9Ye2q3rnVVGmqRICmseWni4=",
  "publicKey": "BABVt+mpRLMXiQpIfEq6bj8hlXsdtXIxLsspmMgLNI1SR5mHgDVbjHO2A+U4QlMddGzqyEidzm1AkhtSxSO2Ahg=",
  "x509PublicKey": "MIIBMTCB6gYHKoZIzj0CATCB3gIBATArBgcqhkjOPQEBAiB/////////////////////////////////////////7TBEBCAqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqYSRShRAQge0Je0Je0Je0Je0Je0Je0Je0Je0Je0Je0JgtenHcQyGQEQQQqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq0kWiCuGaG4oIa04B7dLHdI0UySPU1+bXxhsinpxaJ+ztPZAiAQAAAAAAAAAAAAAAAAAAAAFN753qL3nNZYEmMaXPXT7QIBCANCAAQAVbfpqUSzF4kKSHxKum4/IZV7HbVyMS7LKZjICzSNUkeZh4A1W4xztgPlOEJTHXRs6shInc5tQJIbUsUjtgIY",
  "nonce": "lmXgblZwotx+DfBgKJF0lZXtAXgBEYr5khh79Zytr2Y="
}
```

Let's suppose the above output represents the generated key material of the sender.

### Encryption

Note that the e (encrypt) command also accepts the `<requester-public-key>` (the last argument) in X.509 standard.

```bash
$ ./fidelius-cli e\
  "Wormtail should never have been the Potter cottage's secret keeper."\
  lmXgblZwotx+DfBgKJF0lZXtAXgBEYr5khh79Zytr2Y=\
  6uj1RdDUbcpI3lVMZvijkMC8Te20O4Bcyz0SyivX8Eg=\
  AYhVZpbVeX4KS5Qm/W0+9Ye2q3rnVVGmqRICmseWni4=\
  BAheD5rUqTy4V5xR4/6HWmYpopu5CO+KO8BECS0udNqUTSNo91TIqIIy1A4Vh+F94c+n9vAcwXU2bGcfsI5f69Y=\;
{
  "encryptedData": "pzMvVZNNVtJzqPkkxcCbBUWgDEBy/mBXIeT2dJWI16ZAQnnXUb9lI+S4k8XK6mgZSKKSRIHkcNvJpllnBg548wUgavBa0vCRRwdL6kY6Yw=="
}
```

### Decryption

Note that the d (decrypt) command also accepts the `<sender-public-key>` (the last argument) in X.509 standard.

```bash
$ ./fidelius-cli d\
  pzMvVZNNVtJzqPkkxcCbBUWgDEBy/mBXIeT2dJWI16ZAQnnXUb9lI+S4k8XK6mgZSKKSRIHkcNvJpllnBg548wUgavBa0vCRRwdL6kY6Yw==\
  6uj1RdDUbcpI3lVMZvijkMC8Te20O4Bcyz0SyivX8Eg=\
  lmXgblZwotx+DfBgKJF0lZXtAXgBEYr5khh79Zytr2Y=\
  DMxHPri8d7IT23KgLk281zZenMfVHSdeamq0RhwlIBk=\
  BABVt+mpRLMXiQpIfEq6bj8hlXsdtXIxLsspmMgLNI1SR5mHgDVbjHO2A+U4QlMddGzqyEidzm1AkhtSxSO2Ahg=\;
{
  "decryptedData": "Wormtail should never have been the Potter cottage's secret keeper."
}
```

### The `--filepath` flag

With the `--filepath` flag, the arguments to the CLI can be specified in a file, one argument per line. The CLI will read the file and parse the arguments in order.

```bash
$ cat /path/to/example-params.txt
d
pzMvVZNNVtJzqPkkxcCbBUWgDEBy/mBXIeT2dJWI16ZAQnnXUb9lI+S4k8XK6mgZSKKSRIHkcNvJpllnBg548wUgavBa0vCRRwdL6kY6Yw==
6uj1RdDUbcpI3lVMZvijkMC8Te20O4Bcyz0SyivX8Eg=
lmXgblZwotx+DfBgKJF0lZXtAXgBEYr5khh79Zytr2Y=
DMxHPri8d7IT23KgLk281zZenMfVHSdeamq0RhwlIBk=
BABVt+mpRLMXiQpIfEq6bj8hlXsdtXIxLsspmMgLNI1SR5mHgDVbjHO2A+U4QlMddGzqyEidzm1AkhtSxSO2Ahg=
```

The above example can be executed as follows:

```bash
$ ./fidelius-cli --filepath /path/to/example-parms.txt
{
  "decryptedData": "Wormtail should never have been the Potter cottage's secret keeper."
}
```

## Using Fidelius CLI with other Programming Languages

While Fidelius CLI is a Java implementation, the [`./examples` folder](https://github.com/mgrmtech/fidelius-cli/tree/main/examples) can be perused for guidance on integrating Fidelius CLI (achieved by invoking the binary as a subprocess) in Node JS, Python, Ruby, and PHP codebases.

1. [NodeJS](https://github.com/mgrmtech/fidelius-cli/blob/main/examples/node/index.js)
2. [Python](https://github.com/mgrmtech/fidelius-cli/blob/main/examples/python/main.py)
3. [Ruby](https://github.com/mgrmtech/fidelius-cli/blob/main/examples/ruby/main.rb)
4. [PHP](https://github.com/mgrmtech/fidelius-cli/blob/main/examples/php/index.php)

The above examples can be run using the following commands:

```
$ node examples/node/index.js
$ python3 examples/python/main.py
$ ruby examples/ruby/main.rb
$ php examples/php/index.php
```

## Acknowledgement

The core logic for Fidelius CLI was excerpted (and improved upon) from [this project](https://github.com/sukreet/fidelius). As mentioned there, the name Fidelius comes from [Fidelius Charm](https://harrypotter.fandom.com/wiki/Fidelius_Charm), a magic spell used to conceal secrets.

Thanks to [Srinivas Gunti](https://github.com/itnug) for his help with a quick demo on turning a SpringBoot application into a CLI application; and to [Sai Somanath Komanduri](https://github.com/saisk8) for his help in figuring out the corresponding Gradle build quirks and/with the BouncyCastle import.

Thanks to [Ranveer Uppal](https://github.com/mgrmtech/fidelius-cli/commits?author=Ranveer0508) for contributing Python, and PHP example implementations. Thanks to [Sai Somanath Komanduri](https://github.com/mgrmtech/fidelius-cli/commits?author=saisk8) for contributing an example Ruby implementation.
