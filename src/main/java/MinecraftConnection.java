import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockClientSession;
import com.nukkitx.protocol.bedrock.BedrockPacketType;
import com.nukkitx.protocol.bedrock.packet.*;
import com.nukkitx.protocol.bedrock.v388.serializer.LoginSerializer_v388;
import com.nukkitx.protocol.bedrock.v390.Bedrock_v390;
import gophertunnel.GoSlice;
import gophertunnel.GoString;
import gophertunnel.Gophertunnel;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MinecraftConnection {
  private final String localAddress = "0.0.0.0";
  private final int localPort = 12345;
  private final String remoteAddress;
  private final int remotePort;

  private final AtomicBoolean ready = new AtomicBoolean(false);
  private final MinecraftPacketPrinter minecraftPacketPrinter = new MinecraftPacketPrinter();

  private final KeyPair keyPair;

  private final String xuid;
  private final String identity;

  private Consumer<TextPacket> textPacketConsumer = textPacket -> {};

  private final BedrockClientSession serverSession;

  public MinecraftConnection(BotConfig botConfig) {
    remoteAddress = botConfig.minecraftAddress();
    remotePort = botConfig.minecraftPort();

    keyPair = KeyUtils.generateKeyPair();
    var authChainResult =
        Gophertunnel.GOPHERTUNNEL.AuthChain(
            GoString.from(botConfig.minecraftUsername()),
            GoString.from(botConfig.minecraftPassword()),
            GoSlice.fromBytes(KeyUtils.pemKey(keyPair)));
    if (authChainResult.r1 != null) {
      throw new RuntimeException(authChainResult.r1);
    }

    var chainData = authChainResult.r0;
    var address = botConfig.minecraftAddress() + ":" + botConfig.minecraftPort();
    var protocol = Bedrock_v390.V390_CODEC.getProtocolVersion();
    var version = Bedrock_v390.V390_CODEC.getMinecraftVersion();

    var encodeLoginPacketResult =
        Gophertunnel.GOPHERTUNNEL.EncodeLoginPacket(
            GoString.from(chainData),
            GoString.from(address),
            GoSlice.fromBytes(KeyUtils.pemKey(keyPair)),
            protocol,
            GoString.from(version));

    if (encodeLoginPacketResult.r4 != null) {
      throw new RuntimeException(encodeLoginPacketResult.r4);
    }

    xuid = encodeLoginPacketResult.r2;
    identity = encodeLoginPacketResult.r3;
    var loginPacketBytes =
        NativeInteropUtils.bytesFromPtr(encodeLoginPacketResult.r0, encodeLoginPacketResult.r1);
    var loginPacket = new LoginPacket();
    LoginSerializer_v388.INSTANCE.deserialize(
        Unpooled.wrappedBuffer(loginPacketBytes), loginPacket);

    InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);
    BedrockClient serverConnection = new BedrockClient(localSocketAddress);
    serverConnection.bind().join();

    var remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
    serverSession = serverConnection.connect(remoteSocketAddress).join();

    serverSession.setPacketCodec(Bedrock_v390.V390_CODEC);
    serverSession.addDisconnectHandler(this::onDisconnect);

    serverSession.setBatchedHandler(
        (session12, compressed, packets) -> {
          packets.forEach(
              packet -> {
                minecraftPacketPrinter.printReceived(packet);

                if (packet.getPacketType() == BedrockPacketType.SERVER_TO_CLIENT_HANDSHAKE) {
                  var serverToClientHandshakePacket = (ServerToClientHandshakePacket) packet;
                  var key =
                      KeyUtils.getEncryptionKey(serverToClientHandshakePacket.getJwt(), keyPair);
                  serverSession.enableEncryption(key);

                  System.out.println("Sending client to server handshake.");
                  serverSession.sendPacketImmediately(new ClientToServerHandshakePacket());
                }

                if (packet.getPacketType() == BedrockPacketType.RESOURCE_PACKS_INFO) {
                  var response = new ResourcePackClientResponsePacket();
                  response.setStatus(ResourcePackClientResponsePacket.Status.HAVE_ALL_PACKS);
                  System.out.println("Sending resource pack client response: " + response);
                  serverSession.sendPacketImmediately(response);
                }

                if (packet.getPacketType() == BedrockPacketType.RESOURCE_PACK_STACK) {
                  var response = new ResourcePackClientResponsePacket();
                  response.setStatus(ResourcePackClientResponsePacket.Status.COMPLETED);
                  System.out.println("Sending resource pack client response: " + response);
                  serverSession.sendPacketImmediately(response);
                  System.out.println("Minecraft startup complete.");
                  ready.set(true);
                }

                if (packet.getPacketType() == BedrockPacketType.NETWORK_STACK_LATENCY) {
                  var networkPacket = (NetworkStackLatencyPacket) packet;
                  if (networkPacket.isSendBack()) {
                    serverSession.sendPacketImmediately(networkPacket);
                  }
                }

                if (packet.getPacketType() == BedrockPacketType.TEXT) {
                  var textPacket = (TextPacket) packet;
                  if (textPacket.getType() == TextPacket.Type.CHAT
                      && !textPacket.getXuid().equals(xuid)) {
                    textPacketConsumer.accept(textPacket);
                  }
                }
              });
        });

    serverSession.sendPacketImmediately(loginPacket);
  }

  public void setTextPacketConsumer(Consumer<TextPacket> textPacketConsumer) {
    this.textPacketConsumer = textPacketConsumer;
  }

  public void sendMessage(String message) {
    if (!ready.get()) {
      return;
    }
    var textPacket = new TextPacket();
    textPacket.setType(TextPacket.Type.CHAT);
    textPacket.setMessage(message);
    textPacket.setSourceName(identity);
    textPacket.setXuid(xuid);
    serverSession.sendPacketImmediately(textPacket);
  }

  private void onDisconnect(DisconnectReason reason) {
    System.out.println("Server disconnected: " + reason);
  }
}
