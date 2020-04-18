import com.pengrad.telegrambot.model.User;

public class TelegramUtils {
  public static String getFriendlyTelegramName(User user) {
    if (user == null) {
      return "Unknown user";
    }

    if (user.username() != null) {
      return user.username();
    }

    var stringBuilder = new StringBuilder();

    stringBuilder.append(user.firstName());

    if (user.lastName() != null) {
      stringBuilder.append(' ');
      stringBuilder.append(user.lastName());
    }

    return stringBuilder.toString();
  }
}
