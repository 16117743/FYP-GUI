package model;

import ignore.Ignore;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import sample.InterfaceModel;

import java.io.*;
import java.sql.*;

/**
 * Created by user on 24/02/2016.
 */
public class AzureDB implements InterfaceModel{
    private String con;
    private int result;
    private PreparedStatement sqlInsertName = null;
    private PreparedStatement getSong = null;
    private Ignore ignore;
    private Connection connection;
    private ProgressBar progBar;

    public AzureDB(){
        try {
            ignore = new Ignore();
            con = ignore.getCon();
            String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Class.forName( driver );
            connection = DriverManager.getConnection(con);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void test1(){
        //final byte[] fileBytes = null;


        Task task = new Task<Void>() {
            @Override public Void call() {
        try {
            final String query = "select data from UserSongs";
            //where songName = 'song3'
            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            int cnt = 0;
            while (rs.next())
            {
                byte[] fileBytes = rs.getBytes(1);
                OutputStream targetFile=
                    new FileOutputStream(
                        "C:\\test\\fromDB" + Integer.toString(cnt) + ".mp3");

                targetFile.write(fileBytes);
                targetFile.close();
                cnt ++;
                updateProgress(cnt, 6);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


                return null;
            }
        };
        //progBar = new ProgressBar();
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    public void test2(){

        Task task = new Task<Void>() {
            @Override public Void call() {
                final int max = 1000000;
                    try {

                        sqlInsertName = connection.prepareStatement("INSERT INTO UserSongs (S_Id, songName, artistName, dataSize, data , P_Id )" +
                            "VALUES ( ?, ?, ?, ?, ?,?)");

                        File file = new File("C:\\theset\\test4.mp3");

                        sqlInsertName.setInt(1, 6);
                        sqlInsertName.setString( 2, "song6" );
                        sqlInsertName.setString( 3, "artist6" );
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
                    }  catch (SQLException e) {
                        e.printStackTrace();
                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                  //  updateProgress(i, max);
                return null;
            }
        };
        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    public ProgressBar getProgressBar(){
        return progBar;
    }

    @Override
    public void setModel(ProgressBar bar){
        //progBar = bar;
    }
}
