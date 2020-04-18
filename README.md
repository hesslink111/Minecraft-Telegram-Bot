# Minecraft <-> Telegram Bot

Proxies messages between a Minecraft Bedrock (not Java Edition) server and a Telegram group.

Currently only compatible with 1.14.60, protocol version 390.

###Made possible by these libraries:

- https://github.com/Sandertv/gophertunnel
- https://github.com/NukkitX/Protocol

###Requirements

- Linux / MacOS (might be ok on Windows)
- JDK 13
- Xbox live account
- Telegram bot

###Setup

Create a `config.properties` file that has the following info:
```
# Address and port to Minecraft Bedrock (not Java Edition) server.
minecraft.address=XXXXXXXXX
minecraft.port=19132

# Username and password to account used by the bot.
# Yes this is sketchy.
minecraft.username=XXXXXX
minecraft.password=XXXXXX

telegram.bot_token=XXXXXXXXX:YYYYYYYYYYYYYYYYYYY
telegram.bot_username=XXXXXXXXXX

# Group ID from which messages should be forwarded to Minecraft server.
# Telegram bot must be added to this group.
telegram.accepted_group_id=XXXXXXX
```

Run the following commands:
```
$ cd go
$ go build -buildmode=c-shared -o gophertunnel_interop.so gophertunnel_interop.go
$ cd ..
$ ./gradlew run
```
