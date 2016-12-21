import java.io.*;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.sound.midi.*;
//----------------------------------------------------------------
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
//----------------------------------------------------------------

import java.text.DateFormat; 
import java.text.SimpleDateFormat; 
import java.util.regex.Pattern; 
import java.util.regex.PatternSyntaxException; 
import java.util.regex.Matcher; 

//----------------------------------------------------------------
public class ReadTab9
{
  public static void main(String args[]) {
    
   System.out.println("begin");
   
   ArrayList<ArrayList<String>> sFullTab;
   String sPath, sFilter;
   Integer iNbLoops, iTempo, iVolume;
   ArrayList<String> aListeFichiers;
     
 try
 {
   
  iNbLoops = 5;
  iTempo = 60;
  iVolume=100;
  
  // lecture des arguments en entrée : répertoire et filtre
  if (args.length == 4) {
         sPath = args[0];
         iVolume = Integer.parseInt(args[1]);
         iNbLoops = Integer.parseInt(args[2]);
         iTempo=Integer.parseInt(args[3]);
         
     }
  else {
   // si pas d'argument, retourner le chemin complet du repertoire de travail
      sPath = System.getProperty("user.dir");
     }
    
  System.out.println("Repertoire : " +sPath);
  System.out.println("Nombres de boucles : " + iNbLoops);
  System.out.println("Tempo : " + iTempo);
  // récupération dans une liste des fichiers contenu dans le répertoire  
  aListeFichiers=ListeFichiersRepertoire(sPath);

  // définition d'un timestamp 
  String FORMAT_DATE = "yyyyMMddHHmmss";
     SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
     Calendar c1 = Calendar.getInstance(); // aujourd'hui
     String sTimestamp = sdf.format(c1.getTime());
       
  // parcours de la liste des fichiers
  for(String sFichier: aListeFichiers){
    
    System.out.println("READTAB9" + sPath + File.separator + File.separator + sFichier);
    
    sFullTab = ReadFile(sPath + File.separator + File.separator + sFichier);
   CreateMidiTabFile(sFullTab,sPath, sFichier.substring(0,sFichier.length()-4) +  sTimestamp, iVolume,iNbLoops,iTempo);
   //ConvertTextToImage(sFullTab,"tab.png");
  }
   
 } //try
  catch(Exception e)
 {
  System.out.println("Exception caught " + e.toString());
 } //catch
 
    System.out.println("end ");
  } //main



/*----------------------------------------------------------------------------*/
 public static ArrayList<ArrayList<String>> ReadFile(String filename)
 {
     ArrayList<ArrayList<String>> ArrayTab = new ArrayList<ArrayList<String>>();
     
     try
     {
      String ligne;
         BufferedReader fichier = new BufferedReader(new FileReader(filename));
         
         while ((ligne = fichier.readLine()) != null) 
         {
          //ArrayTab.add(ligne);
          ArrayTab.add(splitString2SingleAlphaArray(ligne));
          System.out.println("Ligne " + ligne);
          
         }
         fichier.close();
        } 

        catch (Exception e) 
        {
         e.printStackTrace();
        }  
    

    return ArrayTab;
    }
 

 /*----------------------------------------------------------------------------*/
 static void CreateMidiTabFile(ArrayList<ArrayList<String>> ArrayTab,String sPath,String sFileName, Integer iVolume, Integer iNbLoops, Integer iTempo)
 {
    
 try
 {
   
   int iPPQ, iDuree ;
   
   iPPQ = 24;
   iDuree = 60000 / (iTempo * iPPQ);
   
   //****  Create a new MIDI sequence with 24 ticks per beat  ****
  Sequence s = new Sequence(javax.sound.midi.Sequence.PPQ,iPPQ);

  //****  Obtain a MIDI track from the sequence  ****
  Track t = s.createTrack();

  //****  General MIDI sysex -- turn on General MIDI sound set  ****
  byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
  SysexMessage sm = new SysexMessage();
  sm.setMessage(b, 6);
  MidiEvent me = new MidiEvent(sm,(long)0);
  t.add(me);

  //****  set tempo (meta event)  ****
  MetaMessage mt = new MetaMessage();
  byte[] bt = {0x02, (byte)0x00, 0x00};
  mt.setMessage(0x51 ,bt, 3);
  me = new MidiEvent(mt,(long)0);
  t.add(me);

  //****  set track name (meta event)  ****
  mt = new MetaMessage();

  String TrackName = new String("Drummer box Track - by JC LACOUR");
  mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
  me = new MidiEvent(mt,(long)0);
  t.add(me);

  //****  set omni on  ****
  ShortMessage mm = new ShortMessage();
  mm.setMessage(0xB0, 0x7D,0x00);
  me = new MidiEvent(mm,(long)0);
  t.add(me);
  //****  set poly on  ****
  mm = new ShortMessage(); 
  mm.setMessage(0xB0, 0x7F,0x00);
  me = new MidiEvent(mm,(long)0); 
  t.add(me);

  
  /**** Parse tab  ****/
  String sNotes = "oOxXfg";
  int iTrick = 0;
  long itrickfla;

  int iNbColonnes = ArrayTab.get(0).size() ;
  String sNote;

  System.out.println("taille tableau :" + iNbColonnes );

  int iLoop=0;
  while (iLoop < iNbLoops)
  {
   for(int j = 0; j < iNbColonnes; j = j+1) 
   { 
    
    
    if ((sNotes.contains(ArrayTab.get(0).get(j)) == true) || (ArrayTab.get(0).get(j).equals("-")))
    {
     //System.out.println("PLAY");
     for(int i = 0; i < 8; i = i+1) 
      {
      sNote=ArrayTab.get(i).get(j);
  
      //System.out.println(sNote);
      
      //si la note figure bien dans la liste des notes autorisées  
      if (sNotes.contains(sNote) == true)
      {
        if (i == 0) // crash
          {
          mm = new ShortMessage();mm.setMessage(0x99,0x31,0x40*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
          //System.out.println(iTrick + " NOTE ON - CC");
          }
            
        if (i == 1) // hit hat
           {
             mm = new ShortMessage();
             
             if (sNote.equals("x"))
             {
               mm.setMessage(0x99,0x2A,0x40*iVolume/100); 
                   me = new MidiEvent(mm,(long)iTrick);t.add(me);
             }
             else if (sNote.equals("X"))
             {              
                   mm.setMessage(0x99,0x2A,0x40*iVolume/100); 
                    me = new MidiEvent(mm,(long)iTrick);t.add(me);
                   //System.out.println(iTrick + " NOTE ON - HH");
                   }   
             else if (sNote.equals("o"))
                   {mm.setMessage(0x99,0x2E,0x35*iVolume/100); 
                    me = new MidiEvent(mm,(long)iTrick);t.add(me);
                   //System.out.println(iTrick + " NOTE ON - OPEN HH");
                   }    
            
                me = new MidiEvent(mm,(long)iTrick);t.add(me);
            }

        
           if (i == 2) // ride
          {
          mm = new ShortMessage();mm.setMessage(0x99,0x33,0x40*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
          //System.out.println(iTrick + " NOTE ON - RD");
          }
                
        if (i == 3) // Caisse claire (SNARE DRUMS)
          {
           if (sNote.equals("x"))
                {
                  mm = new ShortMessage();mm.setMessage(0x99,0x26,0x40*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
                 //System.out.println(iTrick + " NOTE ON - SD");
                 }
            else if (sNote.equals("g"))
                  {
                  mm = new ShortMessage();mm.setMessage(0x99,0x26,0x25*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
                  //System.out.println(iTrick + " NOTE ON - SD - Ghost note");
                   }
            
              else if (sNote.equals("X"))
                  {
                  mm = new ShortMessage();mm.setMessage(0x99,0x26,0x55*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
                  //System.out.println(iTrick + " NOTE ON - SD - ACCENT");
                   }
              
               else if (sNote.equals("f"))
                  {
                  //mm = new ShortMessage();mm.setMessage(0x99,0x26,0x25); me = new MidiEvent(mm,(long)iTrick/2);t.add(me);
                  //mm = new ShortMessage();mm.setMessage(0x99,0x26,0x40); me = new MidiEvent(mm,(long)iTrick/2);t.add(me);
                  //System.out.println(iTrick + " NOTE ON - SD - FLA");
                   }
          }
        
        if (i == 4) // TOM1
            {
             mm = new ShortMessage();mm.setMessage(0x99,0x32,0x40*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
             //System.out.println(iTrick + " NOTE ON - T1");
             }
        
        if (i == 5) // TOM2
            {
             mm = new ShortMessage();mm.setMessage(0x99,0x30,0x40*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
             //System.out.println(iTrick + " NOTE ON - T2");
             }     
        
         if (i == 6) // FT
            {
             mm = new ShortMessage();mm.setMessage(0x99,0x2D,0x40*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
             //System.out.println(iTrick + " NOTE ON - FT");
             }  
        
        if (i == 7) // Grosse caisse (BASS DRUMS)
              {
          mm = new ShortMessage();mm.setMessage(0x99,0x23,0x50*iVolume/100); me = new MidiEvent(mm,(long)iTrick);t.add(me);
             //System.out.println(iTrick + " NOTE ON - BD");
              }
      } 
     //==============================================================================================
            
              }
             
     /*----------------------------------------------------------------------------*/
     iTrick = iTrick + iDuree ;
     
     //System.out.println(iTrick);
     /*----------------------------------------------------------------------------*/
     
     /*
     
        if (bCC == true)
      {
          mm = new ShortMessage(); mm.setMessage(0x89,0x31,0x00); me = new MidiEvent(mm,(long)iTrick);t.add(me);
          //System.out.println(iTrick + " NOTE OFF - CC");
          bCC = false;
         }
     
     if (bHH == true)
      {
          mm = new ShortMessage(); mm.setMessage(0x89,0x2E,0x00); me = new MidiEvent(mm,(long)iTrick);t.add(me);
          //System.out.println(iTrick + " NOTE OFF - HH");
          bHH = false;
         }
 
     if (bSD == true)
      {
          mm = new ShortMessage(); mm.setMessage(0x89,0x26,0x00); me = new MidiEvent(mm,(long)iTrick);t.add(me);
          //System.out.println(iTrick + " NOTE OFF - SD");
          bSD = false;
         }

     if (bBD == true)
      {
          mm = new ShortMessage(); mm.setMessage(0x89,0x23,0x00); me = new MidiEvent(mm,(long)iTrick);t.add(me);
          //System.out.println(iTrick + " NOTE OFF - BD");
          bBD = false;
         }
     */    
         
     }
          
   }
   iLoop=iLoop+1;
  } // end loop
  
  //***  set end of track (meta event)  ****
  mt = new MetaMessage();
        byte[] bet = {}; // empty array
  mt.setMessage(0x2F,bet,0);
  me = new MidiEvent(mt, (long)300);
  t.add(me);

  //****  write the MIDI sequence to a MIDI file  ****
  /*
  String FORMAT_DATE = "yyyyMMddHHmmss";
     SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
     Calendar c1 = Calendar.getInstance(); // aujourd'hui
     File f = new File("Drum" + sdf.format(c1.getTime())+ ".mid");

     */
  File f = new File(sFileName + ".mid");
         
  MidiSystem.write(s,1,f);
  
 } //try}  
     catch (Exception e) 
     {
         e.printStackTrace();
     }
  }

 /*----------------------------------------------------------------------------*/
 static void ConvertTextToImage(String sTextToConvert,String sFileName) {
        //String text = "partition";

        /*
           Because font metrics is based on a graphics context, we need to create
           a small, temporary image so we can ascertain the width and height
           of the final image
         */
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 12);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(sTextToConvert);
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        g2d.drawString(sTextToConvert, 0, fm.getAscent());
        g2d.dispose();
        try {
          ImageIO.write(img, "png", new File(sFileName)); 
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
 /*--------------------------------------------------------------------------*/
 public static ArrayList<String> splitString2SingleAlphaArray(String s)
 {

  ArrayList<String> sArray = new ArrayList<String>();
 
      if (s == null )
          return null;
       char[] c = s.toCharArray();
     
     
       for (int i = 0; i < c.length; i++)
       {
          sArray.add(String.valueOf(c[i]));
         } 
      return sArray;
 }

 /*--------------------------------------------------------------------------*/
 public static ArrayList<String> ListeFichiersRepertoire(String sPath)
 {
  
  String [] s = new File(sPath).list(); 
  String sFichier;
  ArrayList<String> listeFichiers = new ArrayList<String>(); 
  
  try
  {
    for (int i=0; i<s.length;i++) 
    { 
  
      sFichier=s[i];
      System.out.println("parcours répertoire " + sFichier); 
   
      if(s[i].endsWith(".txt")==true)
      { 
        System.out.println("ajouter fichier TEXTE dans la liste : " + sFichier); 
        listeFichiers.add(s[i]); 
      }
    
    }  
   }
  catch (Exception e) 
     {
         e.printStackTrace();
     }
     return listeFichiers;
 }
/*--------------------------------------------------------------------------*/  
 
 }









