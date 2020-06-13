$(document).ready(function() {
  console.log('ready');
  const protWs = "ws://";
  const prot = "http://";
  const host = '127.0.0.1';
  const port = ':8080';
  const sockBinaryUrl = protWs + host + port + '/binary';
  const sockPicturesUrl = protWs + host + port + '/pictures';
  const sockMjpegUrl = protWs + host + port + '/mjpeg';
  const videoElem = document.getElementById("video");
  const logElem = document.getElementById("log");
  const startElem = document.getElementById("start");
  const stopElem = document.getElementById("stop");
  const canvas = document.getElementById("canvas");
  const output = document.getElementById("output");
  const photo = document.getElementById("photo");
  const myFrameRateSlider = document.getElementById("myFrameRate");
  const myFrameRateElem = document.getElementById("framerate");
  const myScreenSizeRadio = document.getElementsByName("screensize");
  const myBitRateSlider = document.getElementById("myBitRate");
  const myBitRateElem = document.getElementById("bitrate");
  const myPicQualitySlider = document.getElementById("myQuality");
  const myPicQuality = document.getElementById("quality");
  
  var myFrameRate = myFrameRateSlider.value;
  myBitRateElem.innerHTML = myFrameRateSlider.value;
  var myBitRate = myBitRateSlider.value;
  myBitRateElem.innerHTML = myBitRateSlider.value;
  var width = 1280;    
  var height = 720;  
  var picQuality = myPicQualitySlider.value;
  myPicQuality.innerHTML = myPicQualitySlider.value;
  var captureType = "video";

  var recordedChunks = [];
  var streaming = false;
  var pictureCaptureStarted = false;
  var first = true;

  var mediaRecorderOptions = { 
    mimeType: "video/webm",
    videoBitsPerSecond: myBitRate
  };
  
  var displayMediaOptions = {
    video: {
      cursor: "always",
      frameRate: myFrameRate,
      height: height,
      width: width
    },
    audio: false
  };

  myFrameRateSlider.oninput = function() {
    myFrameRateElem.innerHTML = this.value;
    displayMediaOptions['video']['frameRate'] = this.value;
    myFrameRate = this.value;
  } 

  myBitRateSlider.oninput = function() {
    myBitRateElem.innerHTML = this.value;
    mediaRecorderOptions['videoBitsPerSecond'] = this.value;
    myBitRate = this.value;
  } 

  myPicQualitySlider.oninput = function() {
    myPicQuality.innerHTML = this.value;
    picQuality = this.value/100;
  } 

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
    this.button("refresh");
    console.log("captureType " + captureType);
  });

  $('#screesizeradio input[type="radio"]').change( function(e) {
    switch($(this).val()) {
      case "A":
        height = null; width = null;
        break;
      case "B":
        height = 720; width = 1280;
        break;
      case "C":
        height = 1080; width = 1920;
        break;
      default:
        height = null;
        width = null;
    }
    this.button("refresh");
    console.log("height " + height);
    console.log("width " + width);
    displayMediaOptions['video']['height'] = height;
    displayMediaOptions['video']['width'] = width;
  });


  startElem.addEventListener("click", function(evt) {
    startCapture();
  }, false);
  
  stopElem.addEventListener("click", function(evt) {
    pictureCaptureStarted = false;
    stopCapture();
       
  }, false); 

  function startWS(captureType){
    switch(captureType) {
      case "video":
        window.binaryWS = new BinaryWSClient(sockBinaryUrl);
        break;
      case "picture":
        window.binaryWS = new BinaryWSClient(sockPicturesUrl);
        break;
      case "mjpeg":
        window.binaryWS = new BinaryWSClient(sockMjpegUrl);
        break;
      default:
        window.binaryWS = new BinaryWSClient(sockBinaryUrl);
    }
    console.log("ws= " + captureType )
  }

  function stopWS(){
    console.info("stopWS");
    window.binaryWS.disconnect();
    window.binaryWS = null;
  }

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
      switch(captureType) {
        case "video":
          startMediaRecorder();
          dumpOptionsInfo();
          break;
        case "picture":
          pictureCaptureStarted = true;
          startPictureCapture();
          break;
        case "mjpeg":
          pictureCaptureStarted = true;
          startPictureCapture();
          break;
        default:
          break;
      }
      first = true;
      //videoElem.srcObject = await navigator.mediaDevices.getUserMedia(displayMediaOptions);
      
      //videoElem.captureStream = videoElem.captureStream || videoElem.mozCaptureStream;      
    } catch(err) {
      console.error("Error: " + err);
    }
  }


  function startPictureCapture(){
    var context = canvas.getContext('2d');
    if (pictureCaptureStarted == true){
      //console.log("PictureCapture: " + myFrameRate);
      if (width && height) {
        //canvas.width = width;
        //canvas.height = height;
        context.drawImage(video, 0, 0, width, height);
        var data = canvas.toDataURL('image/jpeg', picQuality);
        photo.setAttribute('src', data);
        if (!first){
          canvas.toBlob(function(blob) {
            upload(blob); 
          },'image/jpeg', picQuality);
        }
        first = false; 
      } else {
        context.fillStyle = "#AAA";
        context.fillRect(0, 0, canvas.width, canvas.height);
        var data = canvas.toDataURL('image/jpeg', picQuality);
        photo.setAttribute('src', data);
      }
      setTimeout(startPictureCapture, 1000/myFrameRate);
      }
  }

  function startMediaRecorder(){
    mediaRecorder = new MediaRecorder(videoElem.srcObject, mediaRecorderOptions);
    console.info(mediaRecorder);
    mediaRecorder.ondataavailable = function(event){
      if (event.data.size > 0) {
        recordedChunks.push(event.data);
        if (recordedChunks.length > 0){
          var blob = new Blob(recordedChunks, {
            type: "video/webm"
          });
          upload(blob);  
          recordedChunks = [];
        }
        //download();
      } else {
        // ...
      }
    };
    mediaRecorder.onstop = function(event){
      console.log("mediaRecorder.onstop");
      if (window.binaryWS != null) {
        window.binaryWS.disconnect();
      }
      recordedChunks = [];
    };
    mediaRecorder.start(1000);
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
    console.info("stopCapture");
    let tracks = videoElem.srcObject.getTracks();
    tracks.forEach(track => track.stop());
    videoElem.srcObject = null;
    stopWS(); 
  } 
  function dumpOptionsInfo() {
    const videoTrack = videoElem.srcObject.getVideoTracks()[0];
    console.info("Track settings:");
    console.info(JSON.stringify(videoTrack.getSettings(), null, 2));
    console.info("Track constraints:");
    console.info(JSON.stringify(videoTrack.getConstraints(), null, 2));
  }
  console.log = msg => logElem.innerHTML += `${msg}<br>`;
  console.error = msg => logElem.innerHTML += `<span class="error">${msg}</span><br>`;
  console.warn = msg => logElem.innerHTML += `<span class="warn">${msg}<span><br>`;
  console.info = msg => logElem.innerHTML += `<span class="info">${msg}</span><br>`; 
  console.log('ready');
});