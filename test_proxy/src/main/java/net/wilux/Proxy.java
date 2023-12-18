package net.wilux;

import com.google.common.primitives.Ints;
import com.google.gson.*;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.*;
import net.minecraft.network.packet.s2c.login.*;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

public class Proxy {
    public static class HiddenSerializer<A> implements JsonSerializer<A> {
        @Override
        public JsonElement serialize(A src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive("[...]");
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello test!");

        GsonBuilder gsb = new GsonBuilder();
        gsb.registerTypeAdapter(byte[].class, new HiddenSerializer());
        Gson gson = gsb.setPrettyPrinting().serializeNulls().create();

        try {
            Server server = new Server();
            Client client = new Client();
            var c2sHello = client.start();
            var s2cHello = server.onHello(c2sHello);
            var c2sKey = client.onHello(s2cHello);
            server.onKey(c2sKey);

            System.out.printf("CLIENT: %s\n", gson.toJson(client).formatted());
            System.out.printf("SERVER: %s\n", gson.toJson(server).formatted());

            // MC hardcoded
            EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(s2cHello.getPublicKey().getEncoded());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey cpk = keyFactory.generatePublic(encodedKeySpec);

            System.out.printf("SERVER_keyspec_format: %s\n", encodedKeySpec.getFormat());
            System.out.printf("SERVER_keyspec_algo: %s\n", encodedKeySpec.getAlgorithm());
            System.out.printf("SERVER_keyspec: %s\n", gson.toJson(cpk));

            // od -tx1 < mcpubkey.der
            // openssl rsa -inform der -pubin -in mcpubkey.der -text -noout
            // openssl asn1parse -inform der -in mcpubkey.der -i
            //  as1parse guide: https://www.openssl.org/docs/man1.0.2/man1/asn1parse.html
            //  as1parse https://www.oss.com/asn1/resources/asn1-made-simple/asn1-quick-reference/basic-encoding-rules.html
            byte[] encpub = s2cHello.getPublicKey().getEncoded();
            Files.write(Paths.get("mcpubkey.der"), encpub, StandardOpenOption.CREATE);

            byte[] encsym = client.symmetricKey.getEncoded();
            Files.write(Paths.get("mcsymkey.der"), encsym, StandardOpenOption.CREATE);

            //experiments with modifying encoded PUBKEY
            {
                // does NOT discard trailing bytes
                byte[] xkey = s2cHello.getPublicKey().getEncoded();
                xkey = ArrayUtils.addAll(xkey, (byte)40, (byte)40, (byte)40, (byte)40, (byte)40);
                byte[] xkey_trailing = new X509EncodedKeySpec(xkey).getEncoded();
                Files.write(Paths.get("xkey_tr.der"), xkey_trailing, StandardOpenOption.CREATE);

                // mc impl
                // DOES discard trailing bytes
                PublicKey pk_trailing = NetworkEncryptionUtils.decodeEncodedRsaPublicKey(xkey_trailing);
                byte[] xkey_trailing2 = pk_trailing.getEncoded();
                Files.write(Paths.get("xkey_tr2.der"), xkey_trailing2, StandardOpenOption.CREATE);
            }


        } catch (NetworkEncryptionException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            System.out.printf("ERROR!: %s\n", e);
            return;
        }
        System.out.printf("All ok!\n");
    }
}

