import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

public class main {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", (exchange) -> {
            String html = """
                    <html>
                    <head><title>Docker Java App</title></head>
                    <body>
                        <h1>🚀 Java UI inside Docker</h1>
                        <form action="/hello" method="get">
                            <input type="text" name="name" placeholder="Enter your name"/>
                            <button type="submit">Submit</button>
                        </form>
                    </body>
                    </html>
                    """;

            sendResponse(exchange, html);
        });

        server.createContext("/hello", (exchange) -> {
            String query = exchange.getRequestURI().getQuery();
            String name = "Guest";

            if (query != null && query.startsWith("name=")) {
                name = URLDecoder.decode(query.substring(5), StandardCharsets.UTF_8);
            }

            String response = """
                    <html>
                    <body>
                        <h2>Hello %s 👋</h2>
                        <a href="/">Go Back</a>
                    </body>
                    </html>
                    """.formatted(name);

            sendResponse(exchange, response);
        });

        server.start();
        System.out.println("Server running on http://localhost:8080");
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}