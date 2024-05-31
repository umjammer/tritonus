import java.net.URL;

import org.tritonus.sampled.cdda.CddaURLStreamHandlerFactory;


public class URLTest {

    static {
        URL.setURLStreamHandlerFactory(new CddaURLStreamHandlerFactory());
    }

    public static void main(String[] args) throws Exception {
        String _url = args[0];
        URL url = new URL(_url);
        System.out.println("authority: " + url.getAuthority());
        System.out.println("file: " + url.getFile());
        System.out.println("host: " + url.getHost());
        System.out.println("path: " + url.getPath());
        System.out.println("port: " + url.getPort());
        System.out.println("protocol: " + url.getProtocol());
        System.out.println("query: " + url.getQuery());
        System.out.println("ref: " + url.getRef());
        System.out.println("user info: " + url.getUserInfo());
    }
}
