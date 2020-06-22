# screen-grabber

This application will grab your computer screen from the browser and sends it back to the spring boot server. The process happens in real time through websockets and the result is then saved in server upload_folder.
Note: For the screen to be grabbed, the user will be asked to give sharing permission of some app window or the full desktop screen.

## Features

### Video capture 
This option will send the screen grab in a Webm video format stream, that will be saved on the server folder.
The video quality will be set from Frame rate, Bit rate options and screen size.  This options will also determine the bandwidth used. 

Limitations:
- The frame rate must be greater than 5 (6 at least).


### Picture capture
This option will send screenshots in JPEG format at given frame rate and quality. The images will be saved individually in upload_folder.

Limitations:
- The screen size must be one of the numeric options. Unlike video capture, no original screen size can be used.


### MJPEG capture
This option will send screen shots exactly like the Picture capture, but no images will be saved individually. Instead, a video file will be encoded on the server side and then saved in the upload_folder.

Limitations: 
- The framerate is hardcoded at 6 fps.
- Screen size must be one of the numerical options. - No option for original screen size.
- The user must stop the capture, for the server to flush the muxer and close the file (it takes some seconds for the file to be ready).




## to run:

`docker-compose up`

## to try:

http://127.0.0.1


Tested on: Firefox and Chrome

### Changelog:
- 22/06/20 MJPEG Capture is now working in docker

### Future work:
Optimize the size of websocket messages to better suit the MediaRecorder chunks.
- Picture and MJPEG capture using original screen size.
- Choose the framerate to use in MJPEG capture and maybe the Codec also. Maybe using the first message for the muxer configuration in the server and then digest the following messages for encoding the video.
- Upgrade Ubuntu version in Maven's docker container