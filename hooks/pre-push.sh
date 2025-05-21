#!/bin/bash

echo "🔍 [Git Hook] Running Gradle build before push..."

# 빌드 수행
./gradlew build > /dev/null

# 빌드 실패 시 push 차단
if [ $? -ne 0 ]; then
  echo "❌ Build failed. Push has been blocked."
  exit 1
fi

echo "✅ Build succeeded. Proceeding with push."
