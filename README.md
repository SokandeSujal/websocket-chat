# WebSocket Chat Application

A simple WebSocket chat application built with Java on the server side and HTML, CSS, and JavaScript on the client side. This application allows multiple users to connect, send messages, and chat in real-time.

## Features

- Real-time messaging between clients using WebSocket protocol
- Simple and intuitive user interface
- Broadcasts messages to all connected clients
- Handles user connection and disconnection notifications

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven (optional, for managing dependencies)
- Python 3.x (for serving static files)
- A web browser

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/SokandeSujal/websocket-chat.git
   cd websocket-chat
   ```

2. **Compile the Java server:**

   ```bash
   javac ChatServer.java
   ```

3. **Run the server:**

   ```bash
   java ChatServer
   ```

4. **Serve the front-end files:**

   In a new terminal, navigate to the project root directory and run:

   ```bash
   python -m http.server 3000
   ```

5. **Access the chat application:**

   Open your web browser and go to [http://localhost:3000/index.html](http://localhost:3000/index.html).

## Usage

1. When the chat application loads, you will be prompted to enter a username. If you do not enter one, a random username will be assigned.
2. Type your message in the input field and press **Enter** or click the send button to send your message.
3. All connected clients will see your message, as well as notifications when users join or leave the chat.

## Code Structure

```
project-root/
├── public/
│   ├── index.html        # HTML file for the client interface
│   ├── styles.css        # CSS styles for the chat application
│   ├── script.js         # JavaScript for WebSocket connection and UI logic
│   └── favicon.ico       # Optional favicon for the web application
├── ChatServer.java       # Java WebSocket server implementation
```

## Technologies Used

- Java
- WebSocket
- HTML
- CSS
- JavaScript

## Contributing

Contributions are welcome! If you would like to contribute, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/YourFeature`).
3. Make your changes and commit them (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

- [WebSocket Protocol](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API)
- [Java Networking](https://docs.oracle.com/javase/tutorial/networking/)

## Contact

For any questions or feedback, feel free to reach out:

- GitHub: [your-username](https://github.com/SokandeSujal)
