# ZMote Binding

This binding allows you to control your [ZMote](http://www.zmote.io) device and send 
IR codes to IR enabled gadgets.

## Supported Things

ZMote devices with firmware version 2.

## Discovery

Auto discovery is supported by sending a UDP multicast to all ZMote devices on the 
local network. If your device is not on your local network, you have to configure it 
manually as specified in the Thing configuration section.

## IR Configuration

The IR code configuration is based on a configuration file you can download from 
the ZMote webapp. Just configure your IR remotes with the webapp and download the configuration 
in JSON format. The full path to the configuration file has to be specified in the Thing configuration.
To reference a button, use the value provided for the "key" field.

## Thing Configuration

Currently only the IR transmitter is supported. 

When configuring your device manually, be aware that some options have to be provided 
as numbers, i.e. without quotation marks.

```
zmote:zmt2:devicename [ uuid="CI00a1b2c4", configFile="/path/to/config.json", url="http://10.10.10.10", retry=1, timeout=10 ]
```

- **uuid** (required): The unique ID of your ZMote device. You can get this id by checking 
  the auto-discovered things in your inbox where the uuid will be used as device name.
- **configFile** (required): The configuration file which contains the IR configuration. 
  This *has to be set manually* once the device got discovered.
- **url** (required): The URL of the device. Right now, this value will always be set 
  by device auto-discovery.
- **retry** (optional): The number of retries in case the device is busy (Default: 
  1).
- **timeout** (optional): The time we wait in seconds until we give up connecting to 
  the device (Default: 10).


## Item Configuration

## Channels

### Channel: online

This channel allows you to check the if the ZMote transmitter is currently online. 
Auto discovery will try to find the device in one minute intervals.

**demo.things**
```
zmote:zmt2:sony "Sony Remote" [ uuid="CI00a1b2c4", configFile="/path/to/config.json", url="http://10.10.10.10" ]
```

**demo.items**
```xtend
Switch zmote_sony "ZMote Online" { channel="zmote:zmt2:sony:online" }
```

### Channel: sendkey

Allows you to send the IR code for the given button name, as referenced by the "key" 
field in your configuration file.

**demo.things**
```
zmote:zmt2:sony "Sony Remote" [ uuid="CI00a1b2c4", configFile="/path/to/config.json", url="http://10.10.10.10" ]
```

**demo.items**
```xtend
String zmote_sony_sendkey "Sony Remote Button" { channel="zmote:zmt2:sony:sendkey" }
```

**demo.sitemap**
```xtend
Switch item=zmote_sony_sendkey label="Power" mappings=[ "KEY_POWER"="On" ]
Switch item=zmote_sony_sendkey label="Play/Stop" mappings=[ "KEY_PLAY"="On", "KEY_STOP"="Off" ]
Selection item=zmote_sony_sendkey label="Input" mappings=["KEY_HDMI1"="TV", "KEY_HDMI2"="DVD", "KEY_HDMI3"="X-Box", "KEY_HDMI4"="Playstation" ]
```

### Channel: sendcode

Allows you to send any IR code. This still requires you to provide a configuration 
file at the moment.

**demo.things**
```
zmote:zmt2:sony "Sony Remote" [ uuid="CI00a1b2c4", configFile="/path/to/config.json", url="http://10.10.10.10" ]
```

**demo.items**
```xtend
String zmote_sony_sendcode "IR Code" { channel="zmote:zmt2:sony:sendcode" }
```

**demo.sitemap**
```xtend
Switch item=zmote_sony_sendkey label="Power" mappings=[ "36000,2,1,32,32,64,32,32,64,32,3264"="On" ]
```

