$(document).ready(function() {
  
  console.log('ready');

  var protWs = "ws://"
  var prot = "http://"
  var host = '127.0.0.1'
  var port = ':8080'
  var sockBinaryUrl = protWs + host + port + '/binary';

  window.binaryWS = new BinaryWSClient(sockBinaryUrl);
  window.binaryWS.setOnMessage(showGreetings);
    
  const videoElem = document.getElementById("video");
  const logElem = document.getElementById("log");
  const startElem = document.getElementById("start");
  const stopElem = document.getElementById("stop");

  var myFrameRateSlider = document.getElementById("myFrameRate");
  var myFrameRateElem = document.getElementById("framerate");
  var myFrameRate = 6;

  var myScreenSizeRadio = document.getElementsByName("screensize");
  
  // Options for getDisplayMedia()
  
  var displayMediaOptions = {
    video: {
      cursor: "always",
      frameRate: myFrameRate,
      height: 768,
      width: 1024
    },
    audio: false
  };

  myFrameRateSlider.oninput = function() {
    myFrameRateElem.innerHTML = this.value;
    displayMediaOptions['video']['frameRate'] = this.value;
  } 

  $('input[type="radio"]').change( function(e) {
    switch($(this).val()) {
      case "A":
        height = null;
        width = null;
        break;
      case "B":
        height = 720;
        width = 1280;
        break;
      case "C":
        height = 1080;
        width = 1920;
        break;
      default:
        height = null;
        width = null;
    }
    console.log("height " + height);
    console.log("width " + width);
    displayMediaOptions['video']['height'] = height;
    displayMediaOptions['video']['width'] = width;
  });
  

  var myBitRateSlider = document.getElementById("myBitRate");
  var myBitRateElem = document.getElementById("bitrate");
  var myBitRate = 100000;

  var mediaRecorderOptions = { 
    mimeType: "video/webm",
    videoBitsPerSecond: 1000000
  };

  myBitRateSlider.oninput = function() {
    myBitRateElem.innerHTML = this.value;
    mediaRecorderOptions['videoBitsPerSecond'] = this.value;
  } 


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

  var recordedChunks = [];
  //console.log(stream);
  

  async function startCapture() {
    logElem.innerHTML = "";
    if (window.binaryWS.ws) {
      window.binaryWS.connect();
    }
    try {
      videoElem.srcObject = await navigator.mediaDevices.getDisplayMedia(displayMediaOptions);
      //videoElem.srcObject = await navigator.mediaDevices.getUserMedia(displayMediaOptions);
      dumpOptionsInfo();
      //videoElem.captureStream = videoElem.captureStream || videoElem.mozCaptureStream;
      mediaRecorder = new MediaRecorder(videoElem.srcObject, mediaRecorderOptions);
      console.info(mediaRecorder);
      mediaRecorder.ondataavailable = handleDataAvailable;
      mediaRecorder.onstop = handleStop;
      mediaRecorder.start(1000);
      /*
      mediaRecorder.start();
      setTimeout(event => {
        console.log("stopping");
        mediaRecorder.stop();
      }, 3000);*/
    } catch(err) {
      console.error("Error: " + err);
    }
  } 

function handleStop(event){
  console.log("handleStop");
  if (window.binaryWS.ws != null) {
    window.binaryWS.disconnect();
  }
}

function handleDataAvailable(event) {
  if (event.data.size > 0) {
    recordedChunks.push(event.data);
    if (recordedChunks.length > 1){
      uploadChunks(); // works!!
    }
    //download();
  } else {
    // ...
  }
}

function uploadChunks(){// works!
  var blob = new Blob(recordedChunks, {
    type: "video/webm"
  });
  upload(blob);  
  recordedChunks = [];
}

function upload(data){
  console.log("uploading chunk with size: " + data.size);
  if (! window.binaryWS.ws) {
    window.binaryWS.connect();
  }
  window.binaryWS.ws.send(data);
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

});