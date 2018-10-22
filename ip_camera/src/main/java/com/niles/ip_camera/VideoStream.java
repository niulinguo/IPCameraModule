package com.niles.ip_camera;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niles
 * Date 2018/10/11 10:53
 * Email niulinguo@163.com
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class VideoStream {

    private static final String MIMETYPE_VIDEO_AVC = "video/avc";
    private static final String ENTER = "\r\n";
    private static final MediaCodec.BufferInfo sBufferInfo = new MediaCodec.BufferInfo();
    private static MediaCodec sMediaCodec;
    private static BufferedOutputStream sBufferedOutputStream;

    private static String readLine(InputStream inputStream) {
        StringBuilder builder = new StringBuilder();
        byte[] buf = new byte[1];
        try {
            while (inputStream.read(buf) != -1) {
                String string = new String(buf, "GBK");
                if ("\r".equals(string)) {
                    if (inputStream.read(buf) != -1) {
                        string += new String(buf, "GBK");
                        if (ENTER.equals(string)) {
                            return builder.toString();
                        }
                    }
                }
                builder.append(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static void showVideo(final VideoStreamConfig config) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                rtsp(config);
            }
        }).start();
    }

    private static void showFrame(byte[] data) {
        int inIndex = sMediaCodec.dequeueInputBuffer(5);

        if (inIndex >= 0) {
            ByteBuffer buffer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                buffer = sMediaCodec.getInputBuffer(inIndex);
            } else {
                buffer = sMediaCodec.getInputBuffers()[inIndex];
            }
            assert buffer != null;
            buffer.clear();
            buffer.put(data);
            sMediaCodec.queueInputBuffer(inIndex, 0, data.length, 66, 0);
        }

        int outIndex = sMediaCodec.dequeueOutputBuffer(sBufferInfo, 0);
        if (outIndex >= 0) {
            sMediaCodec.releaseOutputBuffer(outIndex, true);
        }
    }

    private static void stop() {
        if (sBufferedOutputStream != null) {
            try {
                sBufferedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                sBufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sBufferedOutputStream = null;
        }
        if (sMediaCodec != null) {
            try {
                sMediaCodec.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sMediaCodec.release();
            sMediaCodec = null;
        }
    }

    private static void init(Surface surface, int width, int height) throws IOException {
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, width, height);
        sMediaCodec = MediaCodec.createDecoderByType(MIMETYPE_VIDEO_AVC);
        sMediaCodec.configure(mediaFormat, surface, null, 0);
        sMediaCodec.start();
    }

    private static void initOutputStream(VideoStreamConfig config) {
        File mediaFile = createMediaFile(config);
        try {
            sBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(mediaFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void rtsp(VideoStreamConfig config) {
        boolean success = false;
        String resultMessage = "";
        try {
            Socket socket = new Socket(config.getIP(), config.getPort());
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), "GBK");
            writer.write("GET http://" + config.getIP() + ":" + config.getPort() + "/livestream/" + config.getChannel() + "?action=play&media=" + config.getStreamType() + " HTTP/1.1" + ENTER);
            writer.write("User-Agent: " + "HiIpcam/V100R003 VodClient/1.0.0" + ENTER);
            writer.write("Connection: " + "Keep-Aliven" + ENTER);
            writer.write("Cache-Control: " + "no-cache" + ENTER);
            writer.write("Authorization: " + config.getUsername() + " " + config.getPassword() + ENTER);

            String content = "Cseq: 1" + ENTER;
            content += "Transport: RTP/AVP/TCP;unicast;interleaved=0-1" + ENTER + ENTER;

            writer.write("Content-Length: " + String.valueOf(content.length()) + ENTER);
            writer.write(ENTER);

            writer.write(content);
            writer.flush();

            InputStream inputStream = socket.getInputStream();
            String line = readLine(inputStream);
            Log.e("http", line);
            if (!"HTTP/1.1 200 OK".equals(line)) {
                return;
            }
            while (!"".equals(line = readLine(inputStream))) {
                Log.e("http", line);
            }
            Log.e("http", " ");
            int h264Width = 0;
            int h264Height = 0;
            while (!"".equals(line = readLine(inputStream))) {
                Log.e("http", line);
                if (line.startsWith("m=video")) {
                    String[] split = line.split(" ");
                    split = split[2].split("/");
                    h264Width = Integer.parseInt(split[2]);
                    h264Height = Integer.parseInt(split[3]);
                }
            }
            Log.e("http", " ");

            init(config.getSurface(), h264Width, h264Height);
            initOutputStream(config);

            byte[] rtspHead = new byte[8];
            int h264Ts = 0;
            List<byte[]> h264DataList = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < config.getDuration() * 1000) {
                waitAvailable(inputStream, rtspHead.length);
                if (inputStream.read(rtspHead) != rtspHead.length) {
                    throw new RuntimeException("Read RTSP Header Failure");
                }
                ByteOrder bigEndian = ByteOrder.BIG_ENDIAN;
                ByteBuffer byteBuffer = ByteBuffer.wrap(rtspHead, 0, rtspHead.length).order(bigEndian);
                char daollar = (char) byteBuffer.get();
                char channelId = (char) byteBuffer.get();
                int resv = byteBuffer.getShort() & 0xffff;
                int payloadLen = byteBuffer.getInt();
                byte[] rtpData = new byte[payloadLen];
                waitAvailable(inputStream, rtpData.length);
                if (inputStream.read(rtpData) != rtpData.length) {
                    throw new RuntimeException("Read RTP Header Failure");
                }
                byteBuffer = ByteBuffer.wrap(rtpData, 0, rtpData.length).order(bigEndian);

                int b1 = byteBuffer.get() & 0xff;
                byte b2 = byteBuffer.get();
                int pt = b2 & 0x7f;
                int seqno = byteBuffer.getShort() & 0xffff;
                int ts = byteBuffer.getInt();
                long ssrc = byteBuffer.getInt() & 0xffffffffL;

                byte[] data = new byte[payloadLen - 12];
                byteBuffer.get(data);

                switch (pt) {
                    case 96: {
                        // H.264
                        if (h264Ts != ts) {
                            if (!h264DataList.isEmpty()) {
                                byte[] imageData = mergeData(h264DataList);
                                if (sBufferedOutputStream != null) {
                                    sBufferedOutputStream.write(imageData);
                                }
                                try {
                                    if (sMediaCodec != null) {
                                        showFrame(imageData);
                                    }
                                } catch (Exception ignore) {
                                }
                            }
                            h264Ts = ts;
                            h264DataList.clear();
                        }
                        h264DataList.add(data);
                        break;
                    }
                }
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            resultMessage = e.getMessage();
        } finally {
            stop();
            if (success) {
                config.getVideoResultCallback().onSuccess(config.getVideoFile());
            } else {
                config.getVideoResultCallback().onFailure(resultMessage);
            }
        }
    }

    private static byte[] mergeData(List<byte[]> dataList) {
        int length = 0;
        for (byte[] bytes : dataList) {
            length += bytes.length;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        for (byte[] bytes : dataList) {
            byteBuffer.put(bytes);
        }
        return byteBuffer.array();
    }

    private static void waitAvailable(InputStream inputStream, int count) {
        try {
            while (inputStream.available() < count) {
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File createMediaFile(VideoStreamConfig config) {
        File file = config.getVideoFile();
        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Can Not Create Dir " + dir.getAbsolutePath());
        }
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("File Already Exists " + file.getAbsolutePath());
        }
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new RuntimeException("Can Not Create File " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
