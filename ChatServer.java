import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class ChatServer {
    private static final int PORT = 8080;
    private static final Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public static void main(String[] args) {
        System.out.println("WebSocket Chat server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected from: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void broadcastMessage(String message, ClientHandler sender) {
        // Create a JSON-like message format
        String formattedMessage = String.format("{\"sender\":\"%s\",\"message\":\"%s\"}",
                sender != null ? sender.getClientName() : "Server",
                message);

        byte[] messageBytes = encodeWebSocketFrame(formattedMessage);
        for (ClientHandler client : clientHandlers) {
            if (client != sender) { // Send to all other clients
                client.sendBytes(messageBytes);
            }
        }

        // Also send back to the sender so they see their own message
        if (sender != null) {
            sender.sendBytes(messageBytes);
        }
    }

    private static byte[] encodeWebSocketFrame(String message) {
        byte[] payloadData = message.getBytes(StandardCharsets.UTF_8);
        int payloadLength = payloadData.length;

        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(0x81); // Text frame (FIN + opcode)

        if (payloadLength <= 125) {
            frame.write(payloadLength);
        } else if (payloadLength <= 65535) {
            frame.write(126);
            frame.write((payloadLength >> 8) & 0xFF);
            frame.write(payloadLength & 0xFF);
        } else {
            frame.write(127);
            for (int i = 7; i >= 0; i--) {
                frame.write((int) ((payloadLength >> (8 * i)) & 0xFF));
            }
        }

        try {
            frame.write(payloadData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return frame.toByteArray();
    }

    private static String decodeWebSocketFrame(InputStream in) throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();

        boolean isFinalFrame = (byte1 & 0x80) != 0;
        int opcode = byte1 & 0x0F;
        boolean isMasked = (byte2 & 0x80) != 0;
        int payloadLength = byte2 & 0x7F;

        if (payloadLength == 126) {
            payloadLength = (in.read() << 8) | in.read();
        } else if (payloadLength == 127) {
            payloadLength = 0;
            for (int i = 0; i < 8; i++) {
                payloadLength = (payloadLength << 8) | in.read();
            }
        }

        byte[] maskingKey = new byte[4];
        if (isMasked) {
            in.read(maskingKey, 0, 4);
        }

        byte[] payload = new byte[payloadLength];
        in.read(payload, 0, payloadLength);

        if (isMasked) {
            for (int i = 0; i < payloadLength; i++) {
                payload[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
            }
        }

        return new String(payload, StandardCharsets.UTF_8);
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private OutputStream out;
        private InputStream in;
        private String clientName;
        private boolean handshakeComplete = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public String getClientName() {
            return clientName;
        }

        private void handleHandshake() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            String key = null;

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Sec-WebSocket-Key: ")) {
                    key = line.substring(19);
                    break;
                }
            }

            if (key != null) {
                String acceptKey = generateAcceptKey(key);
                String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                        "Upgrade: websocket\r\n" +
                        "Connection: Upgrade\r\n" +
                        "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
                out.write(response.getBytes(StandardCharsets.UTF_8));
                out.flush();
                handshakeComplete = true;
            }
        }

        private String generateAcceptKey(String key) {
            try {
                String concatenated = key + GUID;
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] hash = md.digest(concatenated.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(hash);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();

                handleHandshake();

                if (handshakeComplete) {
                    clientHandlers.add(this);

                    while (true) {
                        String message = decodeWebSocketFrame(in);
                        if (message == null)
                            break;

                        if (clientName == null) {
                            clientName = message;
                            System.out.println("New user joined: " + clientName);
                            broadcastMessage("has joined the chat", this);
                        } else {
                            System.out.println(clientName + ": " + message);
                            broadcastMessage(message, this);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client " + clientName + ": " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void cleanup() {
            try {
                clientHandlers.remove(this);
                if (clientName != null) {
                    broadcastMessage("has left the chat", this);
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }

        public void sendBytes(byte[] bytes) {
            try {
                if (out != null) {
                    out.write(bytes);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Error sending message to " + clientName + ": " + e.getMessage());
            }
        }
    }
}
