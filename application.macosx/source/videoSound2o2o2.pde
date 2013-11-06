import ddf.minim.spi.*;
import ddf.minim.signals.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.ugens.*;
import ddf.minim.effects.*;
import java.io.*;

Minim       minim;
AudioPlayer player;
FFT         fft;
AudioInput  in;

PImage img;
int points = 5000;
int picCount = 1;
String stringPicCount;
float acc = 1;
float shift = 0;//Shift of the picture in the frame.
float blurEdge = 100;
float[] wiggles = {10, 0.1, 50, 8};
int wiggleCount = 0;
float startMic = 100000000;
boolean mic = false;
boolean changed = false;

Particle[] particle = new Particle[points];

void setup() {
  size(640, 480);
  background(255);
  smooth();
  frameRate(30);
  noStroke();
  minim = new Minim(this);
  String audio = sketchPath("data/sound/Memo-2.mp3");
  player = minim.loadFile(audio);
  player.play();
  mic = false;
  fft = new FFT( player.bufferSize(), player.sampleRate() );
  in = minim.getLineIn(Minim.STEREO, 512);
   
  storeFace();
}

void draw() {
  background(255);
  //println(player.position() - player.length());
  
  if (player.position() > player.length() - 200 && !mic){
     mic = true; 
     startMic = millis();
     println("end of track at " + startMic);
  }
  if (millis() - startMic > 3000 && mic){
    player.rewind();
    player.play();
    mic = false;
    float startMic = 100000000;
    println("restart track");
  }
  
  if (mic){
     useMic();
  }
  else{
     useAudio(); 
  }
  
  showFace();
}

void useMic(){
  float level = map(in.mix.level(), 0, 1, -50, 100);
  float level2 = in.mix.level()*10;
  println("level="+level + "   level2=" + level2);
  if (level > 0){
    returnFace(level2);
   } 
   else if(level < 0){
     expandFace(); 
   }
   
}

void useAudio(){
   fft.forward(player.mix);
   
    float aggr = 0;
    for(int i = 0; i < fft.specSize(); i++){
     aggr += fft.getBand(i);
    }
    float mapAggr = map(aggr, 0, 200, 0, 8);
    if (aggr > 100){
      returnFace(mapAggr);
     } 
    else if(aggr < 100){
       expandFace(); 
     } 
}

void storeFace(){
  try{
      img = loadImage("pics/1.480p.jpg");
      println(img.width + ", " + img.height);
        for (int i=0; i<points; i++){
        boolean rightEdge = false; 
        boolean leftEdge = false;  
        // Pick a random point
        int x = int(random(img.width));
        int y = int(random(img.height));
        int loc = x + y*img.width;
        
        color pix = img.pixels[loc];
        
        float r = norm(red(img.pixels[loc]),0,255);
        float g = norm(green(img.pixels[loc]),0,255);
        float b = norm(blue(img.pixels[loc]),0,255);
        
        float diff = abs(brightness(pix));
        float alpha = alpha(pix);
        fill(b,alpha/2,diff, 100);
        diff = map(diff, 255, 0, 0, 8);
        
        PVector origin = new PVector(random((width/2)-20,(width/2)+20), random((height/2)-20, (height/2)+20));
        origin = new PVector(width/2, height/2);
        origin = new PVector(x+shift,y+56);
        particle[i] = new Particle(origin, diff, pix);
        particle[i].setWiggle(wiggles[wiggleCount]);
      }
   }
 catch(NullPointerException e){
   println("There was an error: " + e);
    exit(); 
 }
}

void showFace(){
  for (int i=0; i<points; i++){
    particle[i].run();
  } 
}

void leaveFace(){
  for (int i=0; i<points; i++){
        particle[i].goBack();
  }
}

void returnFace(float wiggleIn){
  for (int i=0; i<points; i++){
        particle[i].setWiggle(wiggleIn);
        particle[i].goToFace();
  }  
}

void collapseToCenterFace(){
  for (int i=0; i<points; i++){
        particle[i].goToCenter();
  }
}

void expandFace(){
  for (int i=0; i<points; i++){
        particle[i].setWiggle(1);
        particle[i].goToRandom();
  }
}

void setColor(float setThis){
  for (int i=0; i<points; i++){
        particle[i].setColor(color(setThis, setThis, setThis));
  }
}

void changeWiggle(){
  println("wiggle: " + wiggles[wiggleCount]);
  for (int i=0; i<points; i++){
        println(particle[i].getWiggle());
        particle[i].setWiggle(wiggles[wiggleCount]);
  }
  wiggleCount++;
}

void mousePressed() {
  collapseToCenterFace(); 
}

void keyPressed(){
   switch(key){
     case 's':
       for (int x=0; x<=20;x++){
         File pic = new File(sketchPath("out"+x+".jpg"));
         if (!pic.isFile()) {
           save("out"+x+".jpg");
           break;
         } 
       }
     break;
    case 'n':
      stringPicCount = nf(picCount++, 2);
      img = loadImage("pics/" + stringPicCount + ".jpg");
      background(255);
      shift = random(0,width/2);//position of the picture in the frame.
      wiggleCount++;
      storeFace();
      break;  
    case 'c':
      collapseToCenterFace();  
      break;
    case 'r':
      background(255);
      break;  
    case 'l':
      noLoop();
      break;
    case 'q':
      exit();
      break;
   } 
}
