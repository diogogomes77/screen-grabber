# screen-grabber

This app will grab your screen from the browser and sends it back in realtime throught websockets to the springboot server where the feature result is then saved in upload_folder.
For this to work, the user needs to give permission to share the screen (app's window or full desktop).

## Features

### Video capture 
This option will send a Webm video format stream, that will be saved on the server folder.
Frame rate and bit rate options will set the video quality and bandwidth used. 

Limitations:
- The frame rate must be 6 at least.


### Picture capture
This option will send screen shots in JPEG format at given frame rate and quality. The images will be saved individually in upload_folder.

Limitations:
- The screen size must be one of the numeric options. Unlike video capture, no original screen size can be used.


### MJPEG capture
This option will send screen shots exactly like the Picture capture, but no images will be saved individually. Instead, a video file will be encoded on the server side and then saved in the upload_folder.

Limitations: 
- Important! this feature is not prepared to run in JAR without a tweak because "it's not possible to load a native library from inside a jar"  (https://github.com/artclarke/humble-video/issues/112#issuecomment-607157685)
- The framerate is hardcoded at 6 fps.
- Screen size must be one of the numerical options. - No option for original screen size.
- The user must stop the capture, for the server to flush the muxer and close the file (it takes some seconds for the file to be ready).


## to run:

`docker-compose up`

## to try:

http://127.0.0.1


Tested on: Firefox and Chrome

To do in the future: 
- Tweek the size of websocket messages to better suit the MediaRecorder chunks.
- Picture and MJPEG capture using original screen size.
- Choose the framerate to use in MJPEG capture and maybe the Codec also. Maybe usin a first message with this information for configuring the muxer in the server and then digest the following messages for encoding the video.
- Implement https://github.com/adamheinrich/native-utils 