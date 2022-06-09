require "json"
require "securerandom"
require "fileutils"

def fetch_fidelius_version
  gradle_build_file_content = File.read(File.join(File.dirname(__FILE__), "../../build.gradle"))
  gradle_build_file_content[/\d+\.\d+\.\d+/]
end

def exec_fidelius_cli(args)
  bin_path = File.join(File.dirname(__FILE__), "../fidelius-cli-#{fetch_fidelius_version}/bin/fidelius-cli")
  fidelius_command = [bin_path] + args
  result = `#{fidelius_command.join(" ")}`
  begin
    result = JSON.parse(result)
  rescue JSON::ParserError
    puts "ERROR · execFideliusCli · Command: #{args.join(" ")}\n#{result}"
  end
end

def generate_ecdh_key_material
  exec_fidelius_cli(["gkm"])
end

def write_params_to_file(*params)
  file_contents = params.join("\n")
  file_path = File.join(File.dirname(__FILE__), "temp", "#{SecureRandom.uuid}.txt")
  FileUtils.mkdir_p(File.dirname(file_path))
  File.write(file_path, file_contents)
  file_path
end

def remove_file_at_path(file_path)
  File.delete(file_path) if File.exist?(file_path)
end

def encrypt_data(encrypt_params)
  params_file_path = write_params_to_file(
    "e",
    encrypt_params[:stringToEncrypt],
    encrypt_params[:senderNonce],
    encrypt_params[:requesterNonce],
    encrypt_params[:senderPrivateKey],
    encrypt_params[:requesterPublicKey]
  )
  result = exec_fidelius_cli(["-f", params_file_path])
  remove_file_at_path(params_file_path)
  result
end

def decrypt_data(decrypt_params)
  params_file_path = write_params_to_file(
    "d",
    decrypt_params[:encryptedData],
    decrypt_params[:requesterNonce],
    decrypt_params[:senderNonce],
    decrypt_params[:requesterPrivateKey],
    decrypt_params[:senderPublicKey]
  )
  result = exec_fidelius_cli(["-f", params_file_path])
  remove_file_at_path(params_file_path)
  result
end

def run_example(string_to_encrypt = '{"data": "There is no war in Ba Sing Se!"}')
  requester_key_material = generate_ecdh_key_material
  sender_key_material = generate_ecdh_key_material

  puts(JSON.pretty_generate({
    requesterKeyMaterial: requester_key_material,
    senderKeyMaterial: sender_key_material,
  }))

  encryption_result = encrypt_data({
    stringToEncrypt: string_to_encrypt,
    senderNonce: sender_key_material["nonce"],
    requesterNonce: requester_key_material["nonce"],
    senderPrivateKey: sender_key_material["privateKey"],
    requesterPublicKey: requester_key_material["publicKey"],
  })

  encryption_result_with_x509_public_key = encrypt_data({
    stringToEncrypt: string_to_encrypt,
    senderNonce: sender_key_material["nonce"],
    requesterNonce: requester_key_material["nonce"],
    senderPrivateKey: sender_key_material["privateKey"],
    requesterPublicKey: requester_key_material["x509PublicKey"],
  })

  puts(JSON.pretty_generate({
    encryptedData: encryption_result["encryptedData"],
    encryptedDataWithX509PublicKey: encryption_result_with_x509_public_key["encryptedData"],
  }))

  decryption_result = decrypt_data({
    encryptedData: encryption_result["encryptedData"],
    requesterNonce: requester_key_material["nonce"],
    senderNonce: sender_key_material["nonce"],
    requesterPrivateKey: requester_key_material["privateKey"],
    senderPublicKey: sender_key_material["publicKey"],
  })

  decryption_result_with_x509_public_key = decrypt_data({
    encryptedData: encryption_result["encryptedData"],
    requesterNonce: requester_key_material["nonce"],
    senderNonce: sender_key_material["nonce"],
    requesterPrivateKey: requester_key_material["privateKey"],
    senderPublicKey: sender_key_material["x509PublicKey"],
  })

  puts("String to encrypt: #{string_to_encrypt}")

  puts(JSON.pretty_generate({
    decryptedData: decryption_result["decryptedData"],
    decryptedDataWithX509PublicKey: decryption_result_with_x509_public_key["decryptedData"],
  }))
end

run_example if __FILE__ == $PROGRAM_NAME
