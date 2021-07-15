# Heimdall
Small general purpose plugin for the Egirls Nation login server.

The plugin was made just for one server which is Egirls Nation, you can use it on other servers, but some features will most probably not work correctly.

Pull requests are welcome!

## Disclaimer

If someone will want to use it on their server I or any of the contributors don't take responsibility for broken servers, dead SDD drives, thermonuclear war, or you loosing your sleep because it didn't work and you had to fix it.

Please do some research, if you have any concerns about features included in this plugin before using it!  
YOU are choosing to make these modifications to your server, and if you point the finger at us for messing up your server, we will laugh at you.

### Description
This plugin is a fork of the mapcha plugin, providing stronger captcha and authme integration.

On player join, the player's inventory is cleared, and they are given an empty map.

On right-click, the captcha will show. From there the player will have a fixed amount of time to complete the captcha.

The player also has a limited number of tries. Once the time has reached or the tries limit has been reached the player is kicked.

If the captcha is completed the player's items are returned to them.

### Screenshot (outdated)
![screenshot](https://i.imgur.com/2gK9mEV.png)

### Permissions
* mapcha.bypass
    * Allows the player to bypass the captcha.
  
