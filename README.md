# TCP-SoundPlayer
Sound player controlled by HOTSPOT TCP server

## How to use app
- Set up the HOTSPOT Wifi
- Start the application and go to setting page at top-right corner
- Add the `code` and `sound file`. The application will play this sound file when it's received
  the packet `$PREFIX$code\n`, where `$PREFIX` is implicitly `3.14159` and `code` is the user-defined string.
- Go back to the main application, press the green button. The IP address and Port should be displayed.
- Connect to HOTSPOT phone and send the packet `$PREFIX$code\n` to test.

