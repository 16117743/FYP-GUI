package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.*;

/**
 * Created by user on 13/02/2016.
 */
public class DB {
    public DB() {
    }

    public Connection dbConnect(String db_connect_string,
                                String db_userid, String db_password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    db_connect_string, db_userid, db_password);

            System.out.println("connected");
            return conn;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertImage(Connection conn, String img) {
        int len;
        String query;
        PreparedStatement pstmt;

        try {
            File file = new File(img);
            FileInputStream fis = new FileInputStream(file);
            len = (int) file.length();

            query = ("insert into localsongdatabase VALUES(?,?,?,?)");
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, 1);
            pstmt.setString(2, "song1");
            pstmt.setString(3, "artist1");

            // Method used to insert a stream of bytes
            pstmt.setBinaryStream(4, fis, len);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getImageData(Connection conn) {

        byte[] fileBytes;
        String query;
        try {
            query = "select fileData from localsongdatabase";
            Statement state = conn.createStatement();
            ResultSet rs = state.executeQuery(query);
            if (rs.next()) {
                fileBytes = rs.getBytes(1);
                OutputStream targetFile =
                        new FileOutputStream(
                                "C:\\test\\fromDB.mp3");

                targetFile.write(fileBytes);
                targetFile.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
