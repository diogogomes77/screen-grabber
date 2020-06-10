$(document).ready(function() {
  
  console.log('ready');

  /*
  const binaryStart = document.getElementById("binary-start");
  const binaryStop = document.getElementById("binary-stop");
  const testStart = document.getElementById("test-start");
  const testStop = document.getElementById("test-stop");
*/

  var client, destinationAll, username, users;
  var protWs = "ws://"
  var prot = "http://"
  var host = '127.0.0.1'
  var port = ':8080'
  var sockBinaryUrl = protWs + host + port + '/binary';
  var sockTestUrl = prot + host + port + '/test';
  /*
  var sock; 

    console.log('setClient')
    sock = new SockJS(sockBinaryUrl);
   
    sock.debug = function(str) {
        //$("#debug").append(str + "\n");
        console.log('[debug]\t'+str);
    };

    sock.onopen = function() {
      console.log('open');
      sock.send('test');
    };
 */
  window.textWS = new TextWSClient(sockTestUrl);
  window.textWS.setOnMessage(showGreetings);
  window.binaryWS = new BinaryWSClient(sockBinaryUrl);
  window.binaryWS.setOnMessage(showGreetings);
    
  const videoElem = document.getElementById("video");
  const logElem = document.getElementById("log");
  const startElem = document.getElementById("start");
  const stopElem = document.getElementById("stop");
  
  // Options for getDisplayMedia()
  
  var displayMediaOptions = {
    video: {
      cursor: "always",
      frameRate: 1,
      height: 768,
      width: 1024
    },
    audio: false
  };
  
  // Set event listeners for the start and stop buttons
  startElem.addEventListener("click", function(evt) {
    startCapture();
  }, false);
  
  stopElem.addEventListener("click", function(evt) {
    stopCapture();
  }, false); 
  
  console.log = msg => logElem.innerHTML += `${msg}<br>`;
  console.error = msg => logElem.innerHTML += `<span class="error">${msg}</span><br>`;
  console.warn = msg => logElem.innerHTML += `<span class="warn">${msg}<span><br>`;
  console.info = msg => logElem.innerHTML += `<span class="info">${msg}</span><br>`; 

  //var stream = videoElem.mozCaptureStream;
  var recordedChunks = [];
  //console.log(stream);
  var options = { mimeType: "video/webm; codecs=vp8" };

  async function startCapture() {
    logElem.innerHTML = "";
    try {
      //videoElem.srcObject = await navigator.mediaDevices.getDisplayMedia(displayMediaOptions);
      videoElem.srcObject = await navigator.mediaDevices.getUserMedia(displayMediaOptions);
      
      
      console.log(videoElem.srcObject);
      dumpOptionsInfo();
      
      //videoElem.captureStream = videoElem.captureStream || videoElem.mozCaptureStream;
      console.log(videoElem);
      mediaRecorder = new MediaRecorder(videoElem.srcObject, options);
      console.info(mediaRecorder);
      mediaRecorder.ondataavailable = handleDataAvailable;
      console.info("here");
      mediaRecorder.start();
      // demo: to download after 9sec
      setTimeout(event => {
        console.log("stopping");
        mediaRecorder.stop();
      }, 10000);
    } catch(err) {
      console.error("Error: " + err);
    }

  } 
  
  function stopCapture(evt) {
    let tracks = videoElem.srcObject.getTracks();
  
    tracks.forEach(track => track.stop());
    videoElem.srcObject = null;
  } 
  
  function dumpOptionsInfo() {
    const videoTrack = videoElem.srcObject.getVideoTracks()[0];
   
    console.info("Track settings:");
    console.info(JSON.stringify(videoTrack.getSettings(), null, 2));
    console.info("Track constraints:");
    console.info(JSON.stringify(videoTrack.getConstraints(), null, 2));
  }


function handleDataAvailable(event) {
  console.log("data-available");
  if (event.data.size > 0) {
    recordedChunks.push(event.data);
    console.log(recordedChunks);
    download();
  } else {
    // ...
  }
}

function download() {
  var blob = new Blob(recordedChunks, {
    type: "video/webm"
  });
  var url = URL.createObjectURL(blob);
  var a = document.createElement("a");
  document.body.appendChild(a);
  a.style = "display: none";
  a.href = url;
  a.download = "test.webm";
  a.click();
  window.URL.revokeObjectURL(url);
}

recorderProcess = (e) => {
  const left = e.inputBuffer.getChannelData(0);
  this.socket.emit('stream', this.convertFloat32ToInt16(left))
}


});