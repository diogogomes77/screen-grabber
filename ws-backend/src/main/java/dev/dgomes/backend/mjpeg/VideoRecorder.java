package dev.dgomes.backend.mjpeg;

import dev.dgomes.backend.fileupload.StorageService;
import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static io.humble.video.awt.MediaPictureConverterFactory.convertToType;

public class VideoRecorder {

    private Muxer muxer;
    private StorageService storageService;
    private String filename;
    private MuxerFormat format;
    private String codecname;
    private Codec codec;
    private Encoder encoder;
    private PixelFormat.Type pixelformat;
    private Rational framerate;
    private MediaPicture picture;
    private MediaPictureConverter converter;
    private int i;
    private MediaPacket packet;
    private boolean running = false;

    public VideoRecorder(String filename, StorageService storageService,int snapsPerSecond) {
        this.filename = filename;
        this.storageService = storageService;

        //this.muxer = Muxer.make(filename, null, "mp4");
        this.muxer = storageService.getMuxer(filename);

        format = muxer.getFormat();
        System.out.println("format= " + format);

        if (codecname != null) {
            codec = Codec.findEncodingCodecByName(codecname);
        } else {
            codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
        }
        System.out.println("codec= " + codec);

        framerate = Rational.make(1, snapsPerSecond);
        System.out.println("framerate= " + framerate);
    }


    private void initialize(int height, int width){
        encoder = Encoder.make(codec);
        encoder.setWidth(width);
        encoder.setHeight(height);
        PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
        encoder.setPixelFormat(pixelformat);
        encoder.setTimeBase(framerate);

        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
            encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
        encoder.open(null, null);


        try {
            muxer.addNewStream(encoder);
            muxer.open(null, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        converter = null;
        picture = MediaPicture.make(
                encoder.getWidth(),
                encoder.getHeight(),
                pixelformat);
        picture.setTimeBase(framerate);
        packet = MediaPacket.make();
    }

    public void addPicture(BufferedImage image) {
        //BufferedImage screen = createImageFromBytes(byteChannel.array());
        BufferedImage screen = convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
        if (!running){
            running = true;
            initialize(screen.getHeight(), screen.getWidth());
        }
        if (converter == null)
            System.out.println("converter null");
            converter = MediaPictureConverterFactory.createConverter(screen, picture);
        converter.toPicture(picture, screen, i);
        i++;
        do {
            encoder.encode(packet, picture);
            if (packet.isComplete())
                System.out.println("packet complete");
            try {
                muxer.write(packet, false);
            }catch (Exception e){
               // finishVideo(); // TODO dirty fix
            }
        } while (packet.isComplete());

        //ByteBuffer byteBuffer = ByteBuffer.wrap(byteChannel.array());
        //storageService.storeByteBuffer(byteBuffer,filename);
    }

    public void finishVideo(){
        running = false;
        i = 0;
        System.out.println("finishVideo");
        do {
            encoder.encode(packet, null);
            if (packet.isComplete())
                muxer.write(packet,  false);
        } while (packet.isComplete());
        muxer.close();
    }
}
