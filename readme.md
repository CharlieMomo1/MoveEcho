# MoveEcho

MoveEcho is a real-time location tracking Android application that uses MQTT for lightweight communication between devices. The app allows users to share their location and transportation mode while displaying other users' locations on a map.

![Screenshot 2025-03-12 193221](https://github.com/user-attachments/assets/39b99d7f-1864-470b-87cf-3ce0e3cc8dd1)

## Features

- **Real-time Location Tracking**: Shares user's GPS coordinates via MQTT
- **Transport Type Selection**: Users can specify their mode of transportation (car, bike, truck, motorcycle, walking)
- **Background Location Updates**: Continues to update location when app is in the background
- **Efficient Communication**: Uses lightweight MQTT protocol for real-time data sharing
- **Low Battery Impact**: Optimized to minimize battery usage while tracking location

## Technology Stack

- Kotlin for Android development
- MQTT (Eclipse Paho client) for messaging
- Google Maps API for visualization
- Android Location Services for GPS tracking

## Setup Requirements

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- Android device or emulator running Android 7.0 (API 24) or newer
- MQTT broker running at a specified IP and port
- Kotlin 1.8.0 or newer

### MQTT Broker Configuration
The app connects to an MQTT broker specified in the `MqttHelper` class:

```kotlin
private const val BROKER_URL = "tcp://your-broker-ip:1883"
private const val LOCATION_TOPIC = "moveecho/locations"
```

For testing purposes, you can use a free public MQTT broker or set up your own using:
- Mosquitto on a local server
- HiveMQ or CloudMQTT for cloud hosting

## Getting Started

1. Clone this repository
```
git clone https://github.com/CharlieMomo1/MoveEcho.git
```

2. Open the project in Android Studio

3. Configure your MQTT broker in `MqttHelper.kt`

4. Build and run the app on your Android device or emulator

## Message Format

Location updates are sent in JSON format:

```json
{
  "clientId": "unique-client-id",
  "lat": 37.7749,
  "lng": -122.4194,
  "type": "car", 
  "timestamp": 1646246400000
}
```

## Implementation Details

### Architecture
The app follows a service-based architecture:
- `MainActivity`: User interface for controlling tracking and setting transport type
- `LocationService`: Background service that handles GPS updates
- `MqttHelper`: Manages MQTT connection and messaging
- `NotificationHelper`: Handles foreground service notifications

### Permissions
The app requires the following permissions:
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `ACCESS_BACKGROUND_LOCATION`
- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_LOCATION`

## Future Enhancements

- Map visualization of nearby users
- User authentication and private groups
- Proximity-based filtering
- Route tracking and history
- Battery optimization enhancements
- Transport mode auto-detection

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Eclipse Paho for the MQTT client library
- Google Maps Platform for location services
