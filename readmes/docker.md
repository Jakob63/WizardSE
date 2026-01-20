# Docker

Builden:
```cmd
docker build -t wizard-game-gui .
```

Starten:
```cmd
docker run -it --rm -p 6080:6080 wizard-game-gui
```

Im Terminal kann mit der TUI interagiert werden und unter http://localhost:6080/vnc.html mit der GUI.

Die Infos und Ablauf sind im [Dockerfile](/Dockerfile) zu finden.
Das Dockerfile führt das entrypoint.sh file aus. In diesem werden alle benötigten Dienste gestaret werden.
Hier wird z.B. 

## Virtueller Bildschirm: `Xvfb`
Es simuliert einen Monitor im RAM vom Container, da Docker keinen echten hat. Bzw. keinen Ort hat, an dem unsere GUI "projiziert" werden kann.
Mit Xvfb startet man eine Art virtuellen Bildschirm und an diesen werden dann die Grafikbefehle geschickt.

## Fenstermanager: `fluxbox`
Es sorgt dafür, dass die Fenster vom Spiel auch richtig positioniert sind und maximiert oder minimiert werden können. (bei uns startet es z.B. eher klein)

## Grafiksignale: `VNC-Server` 
Damit man das Bild vom virtuellen Bildschirm auch sieht. Es startet einen `VNC-Server`. Der "streamt" quasi den Inhalt.

## noVNC: `VNC-Proxy`
Wir haben keinen VNC-CLient installieren wollen, deshalb startet dsas Skript den `noVNC Proxy`. Der kann das VNC "Signal" auf eine Art und Weise umleiten, damit man es im Webbrowser eben unter http://localhost:6080/vnc.html sieht.