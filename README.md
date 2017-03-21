# cordova-beacon-background
A simple Android service / plugin for Cordova that monitors BLE beacons in the background. Uses Estimote SDK under the hood.

### Installation

```
cordova plugin add https://github.com/Topl/cordova-beacon-background.git
```

### Disclaimer

You will probably want/need to modify the `BeaconNotificationsManager.java` and `MyService.java` files for your own purposes. In particular,

```
private void updateServer(Region region) 
```
and
```
protected JSONObject getConfig
protected void setConfig
public void startMonitoring()
```

currently uses a very specific set of server requests when differently classified beacons are detected. The main purpose of this repository is solely as a starting point for your own project.

### Usage

Start the sticky service:

```
cordova.plugins.myService.startService(function (result) {
    // do start running
}, function (err) {
    console.log('Error starting service: ', err);
});
```


Set configuration:

```
cordova.plugins.myService.setConfiguration({
    "accessToken": accessToken || '',
    "APIendpoint": 'https://myapi.url.com/',
    "paymentRegions": [
        //UUIDs to monitor as strings
    ],
    "pushRegions": [
        // UUIDs to monitor as strings
    ]
},
function (result) {
    // result of configuration accessible here
},
function (err) {
    console.log('Configuration error: ', err);
});
```


Register for updates:

```
cordova.plugins.myService.registerForUpdates(function (result) {
    // result of register (configuration) accessible
}, function (err) {
    console.error('Error registering for updates: ', err);
});
```


Register for boot start:

```
cordova.plugins.myService.registerForBootStart(function (result) {
    // result of register (configuration) accessible
}, function (err) {
    console.error('Error registering for boot start: ', err);
});
```


Stop the service:

```
cordova.plugins.myService.stopService(function (result) {
    // result of stopping service accessible
}, function (err) {
    console.error('Error stop service after config: ', err);
});
```
