# kandilli-bot

### About

Kandilli Bot is a Discord bot that queries earthquakes in Turkey provided by [Kandilli Observatory](http://www.koeri.boun.edu.tr/sismo/2/en/).
You can invite the bot to your server with this [link](https://discordapp.com/api/oauth2/authorize?client_id=627136746601316392&permissions=10240&scope=bot).

### Commands
* `!deprem help`: displays commands and parameters
* `!deprem`: displays last earthquake
* `!deprem son <count>`: displays last `<count>` earthquakes
* `!deprem büyük <threshold>`: displays last earthquake with magnitude greater than `<threshold>`
* `!deprem son <count> büyük <threshold>`: combines commands above
* `!deprem dürt <threshold>`: watches earthquakes with magnitude greater than `<thresold>` every hour.
* `!deprem dürtme`: stops watch
* `!deprem clear`: deletes messages of bot and commands

**Note**: Display count is limited with **10** records.

### License
This repository is under the [MIT license](https://github.com/yildizan/kandilli-bot/blob/master/LICENSE.md).