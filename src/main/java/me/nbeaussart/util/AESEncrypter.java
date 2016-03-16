package me.nbeaussart.util;

import java.lang.reflect.Field;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class AESEncrypter {


    private static final byte[] SALT = {
            (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
            (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
    };
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 128;
    private Cipher ecipher;
    private Cipher dcipher;
    private SecretKey secret;
    private final String passwd;

    public AESEncrypter(String passPhrase) throws Exception {
        this.passwd=passPhrase;

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);

        secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ecipher.init(Cipher.ENCRYPT_MODE, secret);

        dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = ecipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        dcipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
    }

    public String encrypt(String encrypt) throws Exception {
        byte[] bytes = encrypt.getBytes("UTF8");
        byte[] encrypted = encrypt(bytes);
        return new BASE64Encoder().encode(encrypted);
    }

    public byte[] encrypt(byte[] plain) throws Exception {
        byte[] iv = ecipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ct = ecipher.doFinal(plain);
        byte[] result = new byte[ct.length + iv.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ct, 0, result, iv.length, ct.length);
        return result;
    }

    public String decrypt(String encrypt) throws Exception {
        byte[] bytes = new BASE64Decoder().decodeBuffer(encrypt);
        byte[] decrypted = decrypt(bytes);
        return new String(decrypted, "UTF8");
    }

    public byte[] decrypt(byte[] encrypt) throws Exception {
        byte[] iv = new byte[dcipher.getBlockSize()];
        byte[] ct = new byte[encrypt.length - dcipher.getBlockSize()];
        System.arraycopy(encrypt, 0, iv, 0, dcipher.getBlockSize());
        System.arraycopy(encrypt, dcipher.getBlockSize(), ct, 0, ct.length);


        dcipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        return dcipher.doFinal(ct);
    }

    public static void main(String[] args) throws Exception {

        String message = "MESSAGE";
        String password = "PASSWORD";

        AESEncrypter encrypter1 = new AESEncrypter(password);
        AESEncrypter encrypter2 = new AESEncrypter(password);

        String encrypted1 = encrypter1.encrypt(message);
        String encrypted2 = encrypter2.encrypt(message);

        System.out.println("Display Encrypted from object 1 and 2..why do they differ?" );

        System.out.println(encrypted1) ;
        System.out.println(encrypted2) ;

        System.out.println("Display Each object decrypting its own encrypted msg. Works as expected" );

        System.out.println(encrypter1.decrypt(encrypted1)) ;
        System.out.println(encrypter2.decrypt(encrypted2)) ;

        System.out.println("Attempt to decrypt the each others msg.. will success since I store the SecretKey" );

        System.out.println(encrypter1.decrypt(encrypted2)) ;
        System.out.println(encrypter2.decrypt(encrypted1)) ;

    }

    public String getPassPhrase() {
        return passwd;
    }
}