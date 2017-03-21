# cordova-beacon-background
A simple Android service / plugin for Cordova that monitors BLE beacons in the background

### Installation

```
cordova plugin add https://github.com/Topl/cordova-beacon-background.git
```

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
