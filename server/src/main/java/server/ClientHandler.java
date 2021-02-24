package server;

import commands.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickName;

    public Socket getSocket() {
        return socket;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // цикл аутоинтефикации
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals(Command.END)) {
                            out.writeUTF(Command.END);
                            throw new RuntimeException("Клиент захотел отключиться");
                        }

                        if (str.startsWith(Command.AUTH)) {
                            String[] token = str.split(" ");
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuthService().getNickByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                nickName = newNick;
                                sendMsg(Command.AUTH_OK + " " + nickName);
                                server.subscribe(this);
                                System.out.println("client: " + socket.getRemoteSocketAddress() + " connected with nick: " + nickName);
                                break;
                            } else {
                                sendMsg("Wrong login or password");
                            }
                        }
                    }
                    // цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                out.writeUTF(Command.END);
                                break;
                            }
                            if (str.startsWith("/w")) {
                                String[] text = str.split(" ", 3);
                                if (text.length < 3) {
                                    continue;
                                }
                                server.sendMsgPrivate(this, text[1], text[2]);
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected: " + nickName);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickName() {
        return nickName;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
