package server;

import commands.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickName;
    private String login;
    private static DataAuthentic data;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);
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
                            login = token[1];
                            nickName = DataAuthentic.getNickByParams(token[1], token[2]);
                            System.out.println(nickName);
                            if (newNick != null) {
                                if (!server.isLoginAuthenticated(login)) {
                                    nickName = newNick;
                                    sendMsg(Command.AUTH_OK + " " + nickName);
                                    server.subscribe(this);
                                    System.out.println("client: " + socket.getRemoteSocketAddress() + " connected with nick: " + nickName);
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("Данная учетка уже занята");
                                }

                            } else {
                                sendMsg("Wrong login or password");
                            }
                        }
                        if (str.startsWith(Command.REG)) {
                            String[] token = str.split(" ", 4);
                            if (token.length < 4) {
                                continue;
                            }

                            DataAuthentic.registration(token[1], token[2], token[3]);
                            boolean regSuccess = server.getAuthService().registration(token[1], token[2], token[3]);
                            if (regSuccess) {
                                sendMsg(Command.REG_OK);
                            } else {
                                sendMsg(Command.REG_NO);
                            }
                        }
                    }
                    // цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith(Command.SERVICE_MSG)) {
                            if (str.equals(Command.END)) {
                                out.writeUTF(Command.END);
                                break;
                            }
                            if (str.startsWith(Command.PRIVATE_MSG)) {
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
                } catch (SocketTimeoutException e) {
                    System.out.println("Время вышло");
                    try {
                        out.writeUTF(Command.END);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    DataAuthentic.disconnect();
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

    public String getLogin() {
        return login;
    }
}
