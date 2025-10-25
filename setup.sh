#!/bin/bash

echo "IP Camera Android Project Setup"
echo "================================"
echo ""

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME not set. Please set it to your Android SDK location."
    echo "   Example: export ANDROID_HOME=~/Android/Sdk"
    echo ""
fi

# Create gradle wrapper if it doesn't exist
if [ ! -f "gradlew" ]; then
    echo "📦 Creating Gradle wrapper..."
    gradle wrapper --gradle-version 8.0
fi

echo "✅ Project structure created!"
echo ""
echo "Next steps:"
echo "1. Open Android Studio"
echo "2. Select 'Open an Existing Project'"
echo "3. Navigate to: $(pwd)"
echo "4. Wait for Gradle sync"
echo "5. Connect your Pixel phone via USB"
echo "6. Click Run (Shift+F10)"
echo ""
echo "Or build from command line:"
echo "  ./gradlew assembleDebug"
echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
