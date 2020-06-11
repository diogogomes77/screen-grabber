# screen-grabber

This app will grab your screen from the browser and sends it back in realtime throught websockets to the springboot server where it's then saved on a video file in upload_folder.

to run:

docker-compose up

to access:

http://127.0.0.1


Tested on: Firefox and Chrome

To do: tweek the websocket message sizes to better suit the MediaRecorder chunks.
