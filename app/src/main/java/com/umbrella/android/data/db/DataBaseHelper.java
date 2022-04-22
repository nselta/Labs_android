package com.umbrella.android.data.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "network.db";
    private static String DB_PATH = "";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

        this.getReadableDatabase();
    }

    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            mNeedUpdate = true;
    }

//    //CREATE Network n (id INTEGER PRIMARY KEY, network BLOB);
//    public void Save() { //сохранить
//        File file = new File("path");
//
//        byte[] b = new byte[(int) file.length()];
//        try {
//            FileInputStream fileInputStream = new FileInputStream(file);
//            fileInputStream.read(b);
//            //writeDB(b);
//            System.out.println("Все ок!");
//        } catch (FileNotFoundException e) {
//            System.out.println("File Not Found.");
//            e.printStackTrace();
//        } catch (IOException e1) {
//            System.out.println("Error Reading The File.");
//            e1.printStackTrace();
//        } catch (java.sql.SQLException | ClassNotFoundException throwables) {
//            throwables.printStackTrace();
//        }
//    }

//    public void writeDB(byte[] b) throws ClassNotFoundException, java.sql.SQLException {
//       // Statement stat = conn.createStatement();
//      //  ResultSet rs = stat.executeQuery("select count(*) from people;");
//       // int k = rs.getInt(1);
//        String sql = "Insert into Network(id, network) Values(" + k + ",'" + b + "');";
//        mexecSQL(sql);
////        PreparedStatement pstmt = conn.prepareStatement(sql);
////        pstmt.executeUpdate();
//        System.out.println("Все ок!");
//    }
//
//    public void Update() throws java.sql.SQLException, ClassNotFoundException { //загрузить
//        byte[] res = readDB();
//        try (FileOutputStream fos = new FileOutputStream("pathname")) {
//            fos.write(res);
//            System.out.println("Все ок!");
//        } catch (FileNotFoundException ex) {
//            System.out.println("FileNotFoundException : " + ex);
//        } catch (IOException ioe) {
//            System.out.println("IOException : " + ioe);
//        }
//    }
//
//    public byte[] readDB() throws java.sql.SQLException, ClassNotFoundException {
//        Statement stat = conn.createStatement();
//        ResultSet rs = stat.executeQuery("select * from people;");
//        int size = 0;
//        if (rs != null) {
//            rs.last();
//            size = rs.getRow();
//        }
//        byte[] res = new byte[size];
//        // Перебор строк с данными
//        int i = 0;
//        while (rs.next()) {
//            System.out.println(rs.getByte("id") + " " + rs.getByte("network"));
//            res[i] = rs.getByte("network");
//            i++;
//        }
//        rs.close();
//        //conn.close();
//        return res;
//    }
//
//    public void Delete(int id) throws java.sql.SQLException {
//        String sql = "delete from network where id = " + id + ";";
//        PreparedStatement pstmt = conn.prepareStatement(sql);
//        pstmt.executeUpdate();
//        System.out.println("Все ок!");
//    }
//
//    public StringBuilder SelectAll() throws java.sql.SQLException {
//        Statement stat = conn.createStatement();
//        ResultSet rs = stat.executeQuery("select * from people;");
//        StringBuilder res = null;
//        while (rs.next()) {
//            res.append(rs.getString(1));
//            res.append("\t");
//            res.append(rs.getString(1));
//            res.append("\n");
//        }
//        rs.close();
//        //conn.close();
//        if (res == null) return res.append("В бд нет записей");
//        System.out.println("Все ок!");
//        return res;
//    }
}