package AppPackage;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MainClass 
{
   FileInputStream FIS;
   BufferedInputStream BIS;
   public long pauseLocation;
   public long songTotalLength;
   public String fileLocation;
   
   public Player player;
   
   public void stop()
   {
       if (player != null)
       {
           player.close();
           pauseLocation = 0;
           songTotalLength = 0;
       }
   }
   
   public void Pause()
   {
       if (player != null)
       {
           try 
       {
           pauseLocation = FIS.available();
           player.close();
       } 
          catch (IOException ex) { 
               Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
           } 
       }
   }
   
   public void Resume(){
       try 
       {
           FIS = new FileInputStream(fileLocation);
           BIS = new BufferedInputStream(FIS);
           
           player = new Player(BIS);
           
           FIS.skip(songTotalLength - pauseLocation); // gives us our current location
       } 
       catch (FileNotFoundException | JavaLayerException ex)
       {
           
       } catch (IOException ex) {
         
       } 
      
       new Thread()
       {
           @Override
           public void run()
           {
               try 
               {
                   player.play();
                   //system.out.println("");
               } 
               catch (JavaLayerException javaLayerException) 
               {
                   
               }
           }
       }.start();
   } 
   
   public void Play(String path){
       try 
       {
           
           FIS = new FileInputStream(path);
           BIS = new BufferedInputStream(FIS);
           
           player = new Player(BIS);
           
           songTotalLength = FIS.available(); // calc full length of song
           fileLocation = path + "";
       } 
       catch (FileNotFoundException | JavaLayerException ex)
       {
           
       } catch (IOException ex) {
           Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
       } 
      
       new Thread()
       {
           @Override
           public void run()
           {
               try 
               {
                   player.play();
                   //system.out.println("");
               } 
               catch (JavaLayerException javaLayerException) 
               {
                   
               }
           }
       }.start();
   } 
}
