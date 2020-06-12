$(document).ready(function() {
  
  console.log('ready');

  var protWs = "ws://"
  var prot = "http://"
  var host = '127.0.0.1'
  var port = ':8080'
  var sockBinaryUrl = protWs + host + port + '/binary';
  var sockPicturesUrl = protWs + host + port + '/pictures';
  var sockMjpegUrl = protWs + host + port + '/mjpeg';

  window.binaryWS = new BinaryWSClient(sockBinaryUrl);
  //window.binaryWS.setOnMessage(showGreetings);

  function startWS(captureType){
    if (captureType == "video"){
      window.binaryWS = new BinaryWSClient(sockBinaryUrl);
    }
    else if (captureType == "picture") {
      window.binaryWS = new BinaryWSClient(sockPicturesUrl);
    }
    else if (captureType == "mjpeg") {
      window.binaryWS = new BinaryWSClient(sockMjpegUrl);
    }
    console.log("ws= " + captureType )
  }
  function stopWS(){
    window.binaryWS = null;
  }
    
  const videoElem = document.getElementById("video");
  const logElem = document.getElementById("log");
  const startElem = document.getElementById("start");
  const stopElem = document.getElementById("stop");

  const canvas = document.getElementById("canvas");
  const output = document.getElementById("output");
  const photo = document.getElementById("photo");

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
    myFrameRate = this.value;
  } 
  var width = 1280;    
  var height = 720;     

  $('#screesizeradio input[type="radio"]').change( function(e) {
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

  var myPicQualitySlider = document.getElementById("myQuality");
  var myPicQuality = document.getElementById("quality");

  var picQuality = 0.9;

  myPicQualitySlider.oninput = function() {
    myPicQuality.innerHTML = this.value;
    picQuality = this.value/100;
  } 

  var captureType = "video";

  $('#capturetype input[type="radio"]').change( function(e) {
    switch($(this).val()) {
      case "A":
        captureType = "video";
        $("#myBitRateDiv").fadeIn();
        $('#screenA').fadeIn();
        $("#myQualityDiv").fadeOut();
        break;
      case "B":
        captureType = "picture";
        $("#myBitRateDiv").fadeOut();
        $('#screenA').fadeOut();
        if ($('#screesizeradio input[type="radio"]').val() == 'A'){
          $('#screesizeradio input[type="radio"]').val(['B']);
        }
        $("#myQualityDiv").fadeIn();
        break;
      case "C":
        captureType = "mjpeg";
        $('#screenA').fadeOut();
        if ($('#screesizeradio input[type="radio"]').val() == 'A'){
          $('#screesizeradio input[type="radio"]').val(['B']);
        }
        $("#myQualityDiv").fadeIn();
        break;
      default:
        captureType = "video";
    }
    console.log("captureType " + captureType);
  });
  startElem.addEventListener("click", function(evt) {
    startCapture();
  }, false);
  
  stopElem.addEventListener("click", function(evt) {
    pictureCaptureStarted = false;
    
    stopCapture();
    stopWS();    
  }, false); 
  
  console.log = msg => logElem.innerHTML += `${msg}<br>`;
  console.error = msg => logElem.innerHTML += `<span class="error">${msg}</span><br>`;
  console.warn = msg => logElem.innerHTML += `<span class="warn">${msg}<span><br>`;
  console.info = msg => logElem.innerHTML += `<span class="info">${msg}</span><br>`; 

  var recordedChunks = [];
  //console.log(stream);


  var streaming = false;
  var pictureCaptureStarted = false;

  async function startCapture() {
    logElem.innerHTML = "";
    startWS(captureType);
    if (!window.binaryWS.ws) {
      window.binaryWS.connect();
    }
    try {
      videoElem.srcObject = await navigator.mediaDevices.getDisplayMedia(displayMediaOptions);
      videoElem.addEventListener('canplay', function(ev){
        if (!streaming) {
          //height = video.videoHeight / (video.videoWidth/width);
          //videoElem.setAttribute('width', width);
          //videoElem.setAttribute('height', height);
          canvas.setAttribute('width', width);
          canvas.setAttribute('height', height);
          streaming = true;
        }
      }, false);
     
      if (captureType == "video"){
        startMediaRecorder();
        dumpOptionsInfo();
      } 
      else if (captureType == "picture"){
        pictureCaptureStarted = true;
        startPictureCapture();
      }
      else if (captureType == "mjpeg"){
        pictureCaptureStarted = true;
        startPictureCapture();
      }
      //videoElem.srcObject = await navigator.mediaDevices.getUserMedia(displayMediaOptions);
      
      //videoElem.captureStream = videoElem.captureStream || videoElem.mozCaptureStream;      
    } catch(err) {
      console.error("Error: " + err);
    }
}

function clearphoto() {
  var context = canvas.getContext('2d');
  context.fillStyle = "#AAA";
  context.fillRect(0, 0, canvas.width, canvas.height);

  var data = canvas.toDataURL('image/jpeg', picQuality);
  photo.setAttribute('src', data);
}

function startPictureCapture(){
  if (pictureCaptureStarted == true){
    console.log("PictureCapture: " + myFrameRate);
    var context = canvas.getContext('2d');
    if (width && height) {
      //canvas.width = width;
      //canvas.height = height;
      context.drawImage(video, 0, 0, width, height);
      var data = canvas.toDataURL('image/jpeg', picQuality);
      photo.setAttribute('src', data);
      canvas.toBlob(function(blob) {
        upload(blob); 
      },'image/jpeg', picQuality);
       
    } else {
      clearphoto();
    }
    setTimeout(startPictureCapture, 1000/myFrameRate);
    }
}

function startMediaRecorder(){
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
  console.log('ready');
});