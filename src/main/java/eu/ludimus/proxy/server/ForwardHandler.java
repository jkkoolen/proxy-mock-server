package eu.ludimus.proxy.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.ludimus.proxy.converter.Converter;
import eu.ludimus.proxy.converter.DefaultConverter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Created by jkkoolen on 06/02/15.
 */
public final class ForwardHandler implements HttpHandler {
    public static final int READ_TIMEOUT = 10 * 1000; //
    private Logger logger = LoggerFactory.getLogger(getClass());
    private URL url;
    private String requestMethod;
    private Headers requestHeaders;
    private Converter converter;

    public ForwardHandler(String convertScriptFile) throws IOException {
        this.converter = new DefaultConverter(convertScriptFile);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        url = exchange.getRequestURI().toURL();
        requestMethod = exchange.getRequestMethod();
        requestHeaders = exchange.getRequestHeaders();
        for(Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
            logger.debug("--> " + entry.getKey() + " = " + entry.getValue().toString());
        }
        final String originalRequest = IOUtils.toString(exchange.getRequestBody());
        final String convertedRequest = converter.convertBeforeForward(originalRequest);

        final byte[] convertedResponse = converter.convertBeforeReturn(forward(convertedRequest)).getBytes(Charset.defaultCharset());

        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, convertedResponse.length);

        final OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(convertedResponse);
        responseBody.flush();
        exchange.close();
    }

    private String forward(String value) {
        logger.info(requestMethod + " request to " + url);
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();
        try {
            connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setAllowUserInteraction(true);

            for(Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
                final String headerValue = entry.getValue().toString();
                final String hv = headerValue.substring(1, headerValue.length() - 1);
                if(! hv.isEmpty()) {
                    if ("content-length".equals(entry.getKey().toLowerCase())
                            || "host".equals(entry.getKey().toLowerCase())) { //ignore
                        continue;
                    }
                    connection.setRequestProperty(entry.getKey(), hv);
                }
            }
            connection.setRequestMethod(requestMethod);
            connection.setReadTimeout(READ_TIMEOUT);

            if(value != null && !value.isEmpty()) {
                IOUtils.write(value, connection.getOutputStream());
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } catch(IOException e) {
            logger.error("Error forwarding request", e);
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return response.toString();

    }

}
