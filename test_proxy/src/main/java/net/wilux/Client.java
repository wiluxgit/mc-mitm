package net.wilux;

import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.UUID;

public class Client {
    final String name;
    final UUID uuid;
    SecretKey symmetricKey;

    public Client() {
        name = "Gurkan";
        uuid = UUID.fromString("deadbeef-0123-4567-89ab-06f9ec7ba8f7");
    }
    public LoginHelloC2SPacket start() {
        return new LoginHelloC2SPacket(name, uuid);
    }
    public LoginKeyC2SPacket onHello(LoginHelloS2CPacket packet) throws NetworkEncryptionException {
        symmetricKey = NetworkEncryptionUtils.generateSecretKey();
        PublicKey publicKey = packet.getPublicKey();
        String sid = (new BigInteger(NetworkEncryptionUtils.computeServerId(
                packet.getServerId(),
                publicKey,
                symmetricKey
        ))).toString(16);
        byte[] nonce = packet.getNonce();

        System.out.printf("CLIENT_sid=%s\n", sid);
        return new LoginKeyC2SPacket(symmetricKey, publicKey, nonce);
    }
}
