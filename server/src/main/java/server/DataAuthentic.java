package server;

import java.sql.*;

public class DataAuthentic {
    private static Connection connection;
    private static Statement stm;
    private static PreparedStatement psInsert;
    private static PreparedStatement getNickName;
    private static PreparedStatement registr;



    public static void prepare() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO accounts (login, nick, password) VALUES ( ? , ? , ?);");
        getNickName = connection.prepareStatement("SELECT nick FROM accounts WHERE login = ? AND password = ?;");
    }


    public static String getNickByParams(String  login, String password){

        String nick = null;
        try {
            getNickName.setString(1, login);
            getNickName.setString(2, password);
            ResultSet result = getNickName.executeQuery();
            if (result.next()) {
                nick = result.getString(1);
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
    }

    public static boolean registration(String login, String password, String nickname) {
        try {
            registr.setString(1, login);
            registr.setString(2, password);
            registr.setString(3, nickname);
            registr.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean connect(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            prepare();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void disconnect() {
        try {
            connection.close();
            psInsert.close();
            getNickName.close();
            registr.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            stm.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}



