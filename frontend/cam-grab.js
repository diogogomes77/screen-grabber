$(document).ready(function() {
  console.log('ready');

  const captureVideoButton = $('.capture-button');
  //const captureVideoButton = document.querySelector('.capture-button');
  console.log(captureVideoButton);
  const screenshotBtn = $('#screenshot-button');
  //const screenshotBtn = document.querySelector('#screenshot-button');
  console.log(screenshotBtn);

  const img = document.querySelector('#screenshot img');
  console.log(img);
  const video = document.querySelector('#screenshot video');
  console.log(video);

  const canvas = document.createElement('canvas');
  console.log(canvas);
  console.log('ready');

  captureVideoButton.click( function() {
    console.log('captureVideoButton');
    navigator.mediaDevices.getUserMedia( {video: true}).
      then(stream => {
        video.srcObject = stream;
      }).catch(err => {console.log(err)});
  });

  screenshotBtn.click( function() {
    console.log('screenshotBtn');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    canvas.getContext('2d').drawImage(video, 0, 0);

    let dataURL = canvas.toDataURL('image/png');
    img.src = dataURL;

    var hrefElement = document.createElement('a');
    hrefElement.href = dataURL;
    document.body.append(hrefElement);
    hrefElement.download = 'ScreenShot$.png';
    hrefElement.click();
    hrefElement.remove();
  });




});