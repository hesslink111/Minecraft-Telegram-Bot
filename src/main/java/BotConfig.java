import org.immutables.value.Value;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Value.Immutable
public interface BotConfig {
  String minecraftAddress();

  int minecraftPort();

  String minecraftUsername();

  String minecraftPassword();

  String telegramBotToken();

  String telegramBotUsername();

  Long telegramAcceptedGroupId();

  static BotConfig loadBotConfig() {
    try {
      var properties = new Properties();
      properties.load(new FileInputStream("config.properties"));

      return ImmutableBotConfig.builder()
          .minecraftAddress(properties.getProperty("minecraft.address"))
          .minecraftPort(Integer.parseInt(properties.getProperty("minecraft.port")))
          .minecraftUsername(properties.getProperty("minecraft.username"))
          .minecraftPassword(properties.getProperty("minecraft.password"))
          .telegramBotToken(properties.getProperty("telegram.bot_token"))
          .telegramBotUsername(properties.getProperty("telegram.bot_username"))
          .telegramAcceptedGroupId(
              Long.parseLong(properties.getProperty("telegram.accepted_group_id")))
          .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
