package net.wilux;

import com.google.common.primitives.Ints;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.util.math.random.Random;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyPair;

public class Server {
    final KeyPair keyPair;
    private final byte[] nonce;
    SecretKey symmetricKey;
    public Server() throws NetworkEncryptionException {
        nonce = Ints.toByteArray(Random.create().nextInt());
        keyPair = NetworkEncryptionUtils.generateServerKeyPair();
    }
    public LoginHelloS2CPacket onHello(LoginHelloC2SPacket packet) {
        return new LoginHelloS2CPacket("", this.keyPair.getPublic().getEncoded(), this.nonce);
    }
    public void onKey(LoginKeyC2SPacket packet) throws NetworkEncryptionException {
        SecretKey symmetricKey = packet.decryptSecretKey(this.keyPair.getPrivate());
        String sid = (new BigInteger(NetworkEncryptionUtils.computeServerId(
                "",
                this.keyPair.getPublic(),
                symmetricKey
        ))).toString(16);
        System.out.printf("SERVER_sid=%s\n", sid);
    }
}
