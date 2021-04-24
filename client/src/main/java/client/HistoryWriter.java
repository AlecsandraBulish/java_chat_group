package client;


import java.io.*;

public class HistoryWriter {
    private static BufferedWriter writer;


    private static String getPathHistory(String login) {
        return "history/history_" + login + ".txt";
    }

    public static void start(String login) {
        try {
            writer = new BufferedWriter(new FileWriter(getPathHistory(login), true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeLine(String msg) {

        try {
            writer.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

