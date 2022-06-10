require 'json'
require 'securerandom'
require 'fileutils'

def fetch_fidelius_version
  gradle_build_file_content = File.read(File.join(File.dirname(__FILE__), '../../build.gradle'))
  gradle_build_file_content[/\d+\.\d+\.\d+/]
end

def exec_fidelius_cli(args)
  bin_path = File.join(File.dirname(__FILE__), "../fidelius-cli-#{fetch_fidelius_version}/bin/fidelius-cli")
  fidelius_command = ([bin_path] + args).join(" ")
  result = `#{fidelius_command}`
  begin
    result = JSON.parse(result)
  rescue JSON::ParserError
    puts "ERROR · exec_fidelius_cli · Command: #{args.join(" ")}\n#{result}"
  end
end

def generate_ecdh_key_material
  exec_fidelius_cli(['gkm'])
end

def write_params_to_file(*params)
  file_contents = params.join("\n")
  file_path = File.join(File.dirname(__FILE__), 'temp', "#{SecureRandom.uuid}.txt")
  FileUtils.mkdir_p(File.dirname(file_path))
  File.write(file_path, file_contents)
  file_path
end

def remove_file_at_path(file_path)
  File.delete(file_path) if File.exist?(file_path)
end

def encrypt_data(encrypt_params)
  params_file_path = write_params_to_file(
    'e',
    encrypt_params[:string_to_encrypt],
    encrypt_params[:sender_nonce],
    encrypt_params[:requester_nonce],
    encrypt_params[:sender_private_key],
    encrypt_params[:requester_public_key]
  )
  result = exec_fidelius_cli(['-f', params_file_path])
  remove_file_at_path(params_file_path)
  result
end

def decrypt_data(decrypt_params)
  params_file_path = write_params_to_file(
    'd',
    decrypt_params[:encrypted_data],
    decrypt_params[:requester_nonce],
    decrypt_params[:sender_nonce],
    decrypt_params[:requester_private_key],
    decrypt_params[:sender_public_key]
  )
  result = exec_fidelius_cli(['-f', params_file_path])
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
    string_to_encrypt: string_to_encrypt,
    sender_nonce: sender_key_material['nonce'],
    requester_nonce: requester_key_material['nonce'],
    sender_private_key: sender_key_material['privateKey'],
    requester_public_key: requester_key_material['publicKey'],
  })

  encryption_result_with_x509_public_key = encrypt_data({
    string_to_encrypt: string_to_encrypt,
    sender_nonce: sender_key_material['nonce'],
    requester_nonce: requester_key_material['nonce'],
    sender_private_key: sender_key_material['privateKey'],
    requester_public_key: requester_key_material['x509PublicKey'],
  })

  puts(JSON.pretty_generate({
    encryptedData: encryption_result['encryptedData'],
    encryptedDataWithX509PublicKey: encryption_result_with_x509_public_key['encryptedData'],
  }))

  decryption_result = decrypt_data({
    encrypted_data: encryption_result['encryptedData'],
    requester_nonce: requester_key_material['nonce'],
    sender_nonce: sender_key_material['nonce'],
    requester_private_key: requester_key_material['privateKey'],
    sender_public_key: sender_key_material['publicKey'],
  })

  decryption_result_with_x509_public_key = decrypt_data({
    encrypted_data: encryption_result['encryptedData'],
    requester_nonce: requester_key_material['nonce'],
    sender_nonce: sender_key_material['nonce'],
    requester_private_key: requester_key_material['privateKey'],
    sender_public_key: sender_key_material['x509PublicKey'],
  })

  puts(JSON.pretty_generate({
    decryptedData: decryption_result['decryptedData'],
    decryptedDataWithX509PublicKey: decryption_result_with_x509_public_key['decryptedData'],
  }))
end

run_example if __FILE__ == $PROGRAM_NAME