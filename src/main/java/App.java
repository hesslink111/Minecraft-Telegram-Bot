import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;

public class App {

  private static final BotConfig BOT_CONFIG = BotConfig.loadBotConfig();

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static void main(String[] args) {
    var minecraftConnection = new MinecraftConnection(BOT_CONFIG);
    var telegramConnection = new TelegramConnection(BOT_CONFIG);

    telegramConnection.setTelegramListener(
        message -> {
          System.out.println("Forwarding to minecraft: " + message);
          var friendlyName = TelegramUtils.getFriendlyTelegramName(message.from());
          minecraftConnection.sendMessage(friendlyName + ": " + message.text());
        });

    minecraftConnection.setTextPacketConsumer(
        textPacket -> {
          System.out.println("Forwarding to telegram: " + textPacket);
          var username = textPacket.getSourceName();
          var message = textPacket.getMessage();
          telegramConnection.sendMessage(username + ": " + message);
        });
  }
}
