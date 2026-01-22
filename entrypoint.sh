#!/bin/bash

Xvfb :99 -screen 0 1024x768x24 &
export DISPLAY=:99

sleep 2

fluxbox &
sleep 2

# vDisplay mit VNC freigeben
x11vnc -display :99 -forever -shared -nopw -quiet &

# noVNC Proxy um VNC mit Web-Sockets frei machen
/usr/share/novnc/utils/launch.sh --vnc localhost:5900 --listen 6080 &

java -Djava.awt.headless=false -jar app.jar
