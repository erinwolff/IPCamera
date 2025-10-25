# IP Camera Android App

Turn your Android 11+ phone into an IP camera that streams via MJPEG protocol, viewable in VLC or any browser.

## Features

- MJPEG streaming over HTTP
- 640x480 resolution at 15 FPS
- Works with VLC, browsers, and other MJPEG clients
- Foreground service keeps streaming when app is backgrounded
- Simple one-button start/stop interface

## Requirements

- Android 11 (API 30) or higher
- Camera permission
- WiFi connection (for network streaming)

## Building the App

### Option 1: Using Android Studio (Recommended)

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `IPCamera` folder
4. Wait for Gradle sync to complete
5. Build > Generate App Bundles or APKs > Generate APKs
6. Navigate to output location
7. Connect to Pixel via USB and transfer APK.
8. Install and have fun!
```

## Usage

1. **Install and launch** the app on your Android device
2. **Grant camera permission** when prompted
3. **Connect to WiFi** - note your phone's IP address shown in the app
4. **Press "Start Streaming"**
5. **On your viewing device**, open VLC:
   - Go to Media → Open Network Stream
   - Enter the URL shown in the app (e.g., `http://192.168.1.100:8080/video`)
   - Click "Play"

### Alternative Viewing Methods

**Browser (Chrome/Firefox):**
```
http://192.168.1.100:8080/video
```

**FFmpeg:**
```bash
ffplay http://192.168.1.100:8080/video
```

**mpv:**
```bash
mpv http://192.168.1.100:8080/video
```

## Technical Details

- **Protocol:** MJPEG (Motion JPEG) over HTTP
- **Resolution:** 640x480 (configurable in `CameraHandler.java`)
- **Frame Rate:** 15 FPS (configurable in `MJPEGServer.java`)
- **Port:** 8080 (configurable in constants)
- **JPEG Quality:** 80% (configurable in `CameraHandler.java`)

## Customization

### Change Resolution
Edit `CameraHandler.java`:
```java
private static final int IMAGE_WIDTH = 1280;  // Change from 640
private static final int IMAGE_HEIGHT = 720;  // Change from 480
```

### Change Frame Rate
Edit `MJPEGServer.java`:
```java
private static final int FPS = 30;  // Change from 15
```

### Change Port
Edit both `MainActivity.java` and `StreamingService.java`:
```java
private static final int PORT = 8000;  // Change from 8080
```

### Change JPEG Quality
Edit `CameraHandler.java` in the `imageToJpeg()` method:
```java
yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 
    90, out);  // Change from 80
```

## Troubleshooting

**Can't connect to stream:**
- Ensure phone and viewing device are on the same WiFi network
- Check if your router blocks device-to-device communication
- Verify the IP address shown in the app
- Try disabling any VPN on either device

**Low frame rate:**
- Reduce resolution in `CameraHandler.java`
- Ensure strong WiFi signal
- Close other apps on the phone

**App crashes:**
- Check that camera permissions are granted
- Verify Android version is 11 or higher
- Check logcat for error messages: `adb logcat -s IPCamera`

**Stream is choppy:**
- Lower the JPEG quality setting
- Reduce resolution or frame rate
- Check WiFi bandwidth

## Security Note

This app streams unencrypted video over your local network. For production use, you should:
- Add authentication (username/password)
- Implement HTTPS/TLS encryption
- Add access controls and rate limiting

## License

This is a basic implementation for personal use. Feel free to modify and extend as needed.
