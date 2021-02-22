package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private final int PORT = 8189;
    private ServerSocket server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private List<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthentic();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                System.out.println("client: " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender,String msg){
        String massage = String.format("[ %s ]: %s", sender.getNickName(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(massage);
        }
    }
    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }
    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

}
