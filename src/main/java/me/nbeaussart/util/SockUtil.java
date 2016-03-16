package me.nbeaussart.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by nicbe on 08/03/2016.
 */
public class SockUtil {
    public static void close(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch(IOException e) {
            //...
        }
    }
}
