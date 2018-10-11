package com.niles.ip_camera;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by Niles
 * Date 2018/10/11 10:53
 * Email niulinguo@163.com
 */
public class VideoStream {

    private static final String ENTER = "\r\n";

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

    public static void test(VideoStreamConfig config) {

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
            if (!"HTTP/1.1 200 OK".equals(line)) {
                Log.e("http", line);
                return;
            }
            while (!"".equals(line = readLine(inputStream))) {
                Log.e("http", line);
            }
            byte[] buffer = new byte[1024 * 8];
            while (inputStream.read() != -1) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        HttpURLConnection urlConnection = null;
//        OutputStreamWriter writer = null;
//
//        try {
////            URL url = new URL("http://www.baidu.com");
//            URL url = new URL("http://" + config.getIP() + ":" + config.getPort() + "/livestream/" + config.getChannel() + "?action=play&method=" + config.getStreamType());
//            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.setDoInput(true);
//            urlConnection.setDoOutput(true);
//            urlConnection.setRequestProperty("User-Agent", "HiIpcam/V100R003 VodClient/1.0.0");
//            urlConnection.setRequestProperty("Connection", "Keep-Alive");
//            urlConnection.setRequestProperty("Cache-Control", "no-cache");
//            urlConnection.setRequestProperty("Authorization", config.getUsername() + " " + config.getPassword());
//
//            String content = "Cseq: 1\n";
//            content += "Transport: RTP/AVP/TCP;unicast;interleaved=0-1\n\n";
//
//            urlConnection.setRequestProperty("Content-Length", String.valueOf(content.length()));
//
//            OutputStream outputStream = urlConnection.getOutputStream();
//            writer = new OutputStreamWriter(outputStream, "GBK");
//            writer.write(content);
//            writer.flush();
//
//            urlConnection.connect();
//            String contentEncoding = urlConnection.getContentEncoding();
//            if ("GBK".equalsIgnoreCase(contentEncoding)) {
//
//            }
////            byte[] buff = new byte[1024];
////            inputStream.read(buff);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//
//            if (writer != null) {
//                try {
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//        }
    }
}
