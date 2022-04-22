package com.umbrella.android.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {

    private Connection conn;
    private static String DB_NAME = "network.db";
    private static String DB_PATH = "";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDataBase;
    private boolean mNeedUpdate = false;

//    public DataBase() throws ClassNotFoundException, SQLException {
//        System.out.println("gbfgbgfbfgb");
//        Class.forName("org.sqlite.JDBC");
//        //DriverManager.registerDriver(new org.sqlite.JDBC());
//        this.conn = DriverManager.getConnection("jdbc:sqlite:network.db");
//        System.out.println("fvdfv");
//    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    //CREATE Network n (id INTEGER PRIMARY KEY, network BLOB);
    public void Save() { //сохранить
        /*if ()
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }*/
    }

    public void writeDB(byte[] b) throws ClassNotFoundException, SQLException {
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select count(*) from people;");
        int k = rs.getInt(1);
        String sql = "Insert into Network(id, network) Values(" + k + ",'" + b + "');";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.executeUpdate();
        System.out.println("Все ок!");
    }

    public void Update() throws SQLException, ClassNotFoundException { //загрузить
        byte[] res = readDB();
        try (FileOutputStream fos = new FileOutputStream("pathname")) {
            fos.write(res);
            System.out.println("Все ок!");
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException : " + ex);
        } catch (IOException ioe) {
            System.out.println("IOException : " + ioe);
        }
    }

    public byte[] readDB() throws SQLException, ClassNotFoundException {
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from people;");
        int size = 0;
        if (rs != null) {
            rs.last();
            size = rs.getRow();
        }
        byte[] res = new byte[size];
        // Перебор строк с данными
        int i = 0;
        while (rs.next()) {
            System.out.println(rs.getByte("id") + " " + rs.getByte("network"));
            res[i] = rs.getByte("network");
            i++;
        }
        rs.close();
        //conn.close();
        return res;
    }

    public void Delete(int id) throws SQLException {
        String sql = "delete from network where id = " + id + ";";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.executeUpdate();
        System.out.println("Все ок!");
    }

    public StringBuilder SelectAll() throws SQLException {
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * from people;");
        StringBuilder res = null;
        while (rs.next()) {
            res.append(rs.getString(1));
            res.append("\t");
            res.append(rs.getString(1));
            res.append("\n");
        }
        rs.close();
        //conn.close();
        if (res == null) return res.append("В бд нет записей");
        System.out.println("Все ок!");
        return res;
    }
}
