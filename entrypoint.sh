#!/bin/bash

# Start Xvfb
# virtuelles Display :99
Xvfb :99 -screen 0 1024x768x24 & # Auflösung 1024x768
export DISPLAY=:99

sleep 2

# Window Manager für FX-Apps (ist besser)
sleep 2

# Start x11vnc, vDisplay mit VNC freigeben
# -forever: nach Trennung offen
# -shared: mehrere Connections
# -nopw: Kein Passwort
x11vnc -display :99 -forever -shared -nopw -quiet &

# Start noVNC Proxy um VNC mit Web-Sockets frei machen
# Port 6080 = Standard für noVNC
/usr/share/novnc/utils/launch.sh --vnc localhost:5900 --listen 6080 &

# Start von Wizard
java -Djava.awt.headless=false -jar app.jar
