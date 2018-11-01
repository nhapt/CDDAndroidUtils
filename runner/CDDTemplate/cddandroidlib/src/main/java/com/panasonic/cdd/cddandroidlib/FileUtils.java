package com.panasonic.cdd.cddandroidlib;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by NhaPT <thinha.pham@vn.panasonic.com> on 10/09/2018.
 */
public class FileUtils {

    public static void downloadFile(String fromUrl, File toFile) {
        int count;
        try {
            URL url = new URL(fromUrl);
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            Log.e("start download", "File: "+fromUrl + "\n"+"size: "+conection.getContentLength());

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(toFile);

            byte data[] = new byte[1024];


            while ((count = input.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void deleteFolder(File fileOrDirectory) {
       try {
           if (fileOrDirectory.isDirectory())
               for (File child : fileOrDirectory.listFiles())
                   deleteFolder(child);

           fileOrDirectory.delete();
       } catch (Exception e){
           e.printStackTrace();
       }
    }
}
