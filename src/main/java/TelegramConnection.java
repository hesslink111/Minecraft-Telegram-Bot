import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.request.SendMessage;

import java.time.Instant;
import java.util.Arrays;
import java.util.function.Consumer;

public class TelegramConnection {
  private final TelegramBot bot;
  private final long acceptedGroupId;
  private Consumer<Message> telegramMessageListener = message -> {};

  public TelegramConnection(BotConfig botConfig) {
    acceptedGroupId = botConfig.telegramAcceptedGroupId();
    bot = new TelegramBot(botConfig.telegramBotToken());
    System.out.println("Telegram startup complete.");

    bot.setUpdatesListener(
        updates -> {
          updates.forEach(
              update -> {
                System.out.println("Update:" + update);

                // Basic test reply
                if (update.message() != null && update.message().entities() != null) {
                  var addressedToBot =
                      Arrays.stream(update.message().entities())
                          .anyMatch(
                              entity -> {
                                if (entity.type() != MessageEntity.Type.mention) {
                                  return false;
                                }

                                var offset = entity.offset();
                                var length = entity.length();
                                var username =
                                    update.message().text().substring(offset, offset + length);

                                return username.equals("@" + botConfig.telegramBotUsername());
                              });
                  if (addressedToBot) {
                    var sendMessage = new SendMessage(update.message().chat().id(), ":)");
                    bot.execute(sendMessage);
                  }
                }

                // Send chat messages to minecraft
                if (update.message() != null
                    && update.message().chat() != null
                    && update.message().chat().id() == acceptedGroupId
                    && Instant.now().getEpochSecond() - update.message().date() <= 60 * 5) {
                  System.out.println("Sending message to minecraft");
                  telegramMessageListener.accept(update.message());
                }
              });
          // No commands for now.
          return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
  }

  public void sendMessage(String message) {
    var sendMessage = new SendMessage(acceptedGroupId, message);
    bot.execute(sendMessage);
  }

  public void setTelegramListener(Consumer<Message> listener) {
    this.telegramMessageListener = listener;
  }
}
