package model;

import ignore.Ignore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;

/**
 * Created by user on 24/02/2016.
 */
public class AzureDB {
    private String con;
    private int result;
    private PreparedStatement sqlInsertName = null;
    private Ignore ignore;

    public AzureDB(){
        ignore = new Ignore();
        con = ignore.getCon();
    }

    public void test1(){
        String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

        try {
            Class.forName( driver );
            Connection connection = DriverManager.getConnection(con);

            sqlInsertName = connection.prepareStatement("INSERT INTO UserSongs (S_Id, songName, artistName, dataSize, data , P_Id )" +
                "VALUES ( ?, ?, ?, ?, ?,?)");

            File file = new File("C:\\theset\\test4.mp3");

            sqlInsertName.setInt(1, 5);
            sqlInsertName.setString( 2, "song5" );
            sqlInsertName.setString( 3, "artist5" );
            sqlInsertName.setInt(4, (int) file.length());
            sqlInsertName.setBinaryStream(5, new FileInputStream(file), (int) file.length());
            sqlInsertName.setInt(6, 1);
            result = sqlInsertName.executeUpdate();

            if ( result == 0 ) {
                connection.rollback(); // rollback insert
                System.out.println("roll back");
            }

            connection.commit();
            sqlInsertName.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
         catch (FileNotFoundException e){
             e.printStackTrace();
         }
    }
}
