package com.example.mhdstuff.activity.testing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OverpassDownloader {

    public static String downloadData(String lineRef) {
        try {
            String query = String.format(
                    "[out:json];\n" +
                            "relation[\"network\"=\"IDS JMK\"][\"ref\"=\"%s\"][\"type\"!=\"disused:route\"](49.0928,16.4067,49.3211,16.7953);\n" +
                            "out geom;>;\n" +
                            "node(w)[\"public_transport\"=\"stop_position\"];\n" +
                            "out geom;",
                    lineRef
            );

            URL url = new URL("http://overpass-api.de/api/interpreter");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");

                // IMPORTANT: encode the form value
                String postData = "data=" + URLEncoder.encode(query, "UTF-8");

                // send request body
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] bytes = postData.getBytes(StandardCharsets.UTF_8);
                    os.write(bytes);
                    os.flush();
                }

                // get response code and pick stream accordingly
                int code = conn.getResponseCode();
                InputStream is = (code >= 400) ? conn.getErrorStream() : conn.getInputStream();

                if (is == null) {
                    throw new Exception("No response stream (response code: " + code + ")");
                }

                // read response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    if (code >= 400) {
                        // include status code for debugging
                        throw new Exception("Server returned HTTP " + code + ":\n" + sb.toString());
                    }
                    return sb.toString();
                }
            } finally {
                conn.disconnect();
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
