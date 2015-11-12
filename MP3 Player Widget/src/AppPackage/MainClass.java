package AppPackage;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MainClass 
{
   FileInputStream FIS;
   BufferedInputStream BIS;
   
   public Player player;
   
   public void stop()
   {
       if (player != null)
       {
           player.close();
       }
   }
   
   public void Pause()
   {
       if (player != null)
       {
           try 
       {
           FIS.available();
           BIS = new BufferedInputStream(FIS);
           
           player = new Player(BIS);
       } 
       catch (FileNotFoundException | JavaLayerException ex)
       {
           
       } 
       }
   }
   
   public void Play(String path){
       try 
       {
           FIS = new FileInputStream(path);
           BIS = new BufferedInputStream(FIS);
           
           player = new Player(BIS);
       } 
       catch (FileNotFoundException | JavaLayerException ex)
       {
           
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
