package eu.ludimus.proxy.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by jkkoolen on 06/02/15.
 */
public final class ProxyServer {
    private HttpServer server;
    private static final String USAGE = "ProxyServer <local port> [converterScriptFile]\n\n" +
            "  The converterScriptFile can be used for changing the request before forwarding it with the following keys:\n" +
            "  convertBeforeForward.[N].match for the pattern to match in the original request.\n" +
            "  and convertBeforeForward.[N].to is the new used request\n\n" +
            "  The same can be done for requests just before they are returned with the following keys:\n" +
            "  convertBeforeReturn.[N].match and convertBeforeReturn.[N].to\n\n" +
            "  Where [N] is any number \\d\\d*, e.g. convertBeforeForward.1.match or convertBeforeForward.1432.match";

    private ProxyServer(int port) {
        this(port, null);
    }

    private ProxyServer(int port, String converterScriptFile) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new ForwardHandler(converterScriptFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        server.start();
    }


    public static void main(String[] args) {
        if(!(args.length == 1 || args.length == 2)) {
            System.out.println(USAGE);
            System.exit(2);
        }
        if(args.length == 1) {
            new ProxyServer(Integer.parseInt(args[0])).start();
        } else {
            new ProxyServer(Integer.parseInt(args[0]), args[1]).start();
        }
    }
}
