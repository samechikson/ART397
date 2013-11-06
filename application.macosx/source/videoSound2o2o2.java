import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.spi.*; 
import ddf.minim.signals.*; 
import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.ugens.*; 
import ddf.minim.effects.*; 
import java.io.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class videoSound2o2o2 extends PApplet {









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
float[] wiggles = {10, 0.1f, 50, 8};
int wiggleCount = 0;
float startMic = 100000000;
boolean mic = false;
boolean changed = false;

Particle[] particle = new Particle[points];

public void setup() {
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

public void draw() {
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

public void useMic(){
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

public void useAudio(){
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

public void storeFace(){
  try{
      img = loadImage("pics/1.480p.jpg");
      println(img.width + ", " + img.height);
        for (int i=0; i<points; i++){
        boolean rightEdge = false; 
        boolean leftEdge = false;  
        // Pick a random point
        int x = PApplet.parseInt(random(img.width));
        int y = PApplet.parseInt(random(img.height));
        int loc = x + y*img.width;
        
        int pix = img.pixels[loc];
        
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

public void showFace(){
  for (int i=0; i<points; i++){
    particle[i].run();
  } 
}

public void leaveFace(){
  for (int i=0; i<points; i++){
        particle[i].goBack();
  }
}

public void returnFace(float wiggleIn){
  for (int i=0; i<points; i++){
        particle[i].setWiggle(wiggleIn);
        particle[i].goToFace();
  }  
}

public void collapseToCenterFace(){
  for (int i=0; i<points; i++){
        particle[i].goToCenter();
  }
}

public void expandFace(){
  for (int i=0; i<points; i++){
        particle[i].setWiggle(1);
        particle[i].goToRandom();
  }
}

public void setColor(float setThis){
  for (int i=0; i<points; i++){
        particle[i].setColor(color(setThis, setThis, setThis));
  }
}

public void changeWiggle(){
  println("wiggle: " + wiggles[wiggleCount]);
  for (int i=0; i<points; i++){
        println(particle[i].getWiggle());
        particle[i].setWiggle(wiggles[wiggleCount]);
  }
  wiggleCount++;
}

public void mousePressed() {
  collapseToCenterFace(); 
}

public void keyPressed(){
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
class Particle {
  PVector location, velocity, acceleration, goingTowards, origin, face;
  float limit, diameter, opacity, wiggle, modAcc;
  boolean stopAcc = false;
  int couleur;

  Particle(PVector l, float diam, int col) {
    acceleration = new PVector(-1,1);
    velocity = new PVector(random(-1,-1),random(-1,0));
    origin = new PVector(width/2,height/2);
    location = origin;
    goingTowards = l.get();
    face = l.get();
    limit = 10;
    diameter = diam;
    couleur = col;
    opacity = 100;
    wiggle = 0;
    modAcc = 1;
  }
  
  public void setOrigin(int num){
    if (num == 1){
       location = new PVector(random(-400, 0), random(-200, height+200));
    }
    else if (num == 2){
      location = new PVector(random(-200, width+200), random(-400, 0));
    }
    else if (num == 3){
      location = new PVector(random(width, width+400), random(-200, height+200)); 
    }
    else {
      location = new PVector(random(-200, width+200), random(height, height+400)); 
    }
  }

  public void run() {
    update();
    displayColor();
  }
  
  // Method to update location
  public void update() {
    PVector dir = PVector.sub(goingTowards,location);

    dir.normalize();
    dir.mult(wiggle);
    acceleration = dir;
    
    velocity.add(acceleration);
    velocity.limit(limit);
    location.add(velocity);
  }
  
  public void goBack(){
   goingTowards = origin; 
  }
  
  public void goToFace(){
   goingTowards = face; 
  }
  
  public void goToCenter(){
     goingTowards = new PVector(width/2, height/2); 
  }
  
  public void goToRandom(){
    float randomX = random(-width, width*2);
    float randomY = random(-height, height*2);
    goingTowards = new PVector(randomX, randomY); 
  }
  
  public void setWiggle(float wiggleIn){
     wiggle = wiggleIn;
  }
  
  public float getWiggle(){
    return wiggle; 
  }

  public void setColor(int couleurIn){
    couleur = couleurIn;
  }
  
  public void setLimit(float limitIn){
     limit = limitIn; 
  }
  
  public void modAcc(float accIn){
     modAcc = accIn;
  }
  
  public PVector getLoc(){
     return location; 
  }
  
  public void display() {
    fill(norm(location.x, 0, width)*250,norm(location.x, 0, width)*80,norm(location.y, 0, height)*200,100);
    ellipse(location.x,location.y,diameter,diameter);
  }
  
  public void displayColor() {
    fill(couleur,opacity);
    ellipse(location.x,location.y,diameter,diameter);
  }
  
  public void mouseProx(){
     PVector mouse = new PVector(mouseX, mouseY); 
     PVector proximity = PVector.sub(location, mouse);
     diameter = width/2 - proximity.x;
  }
  
  public PVector getPos(){
     return location; 
  }
  
  public float EdgeProx(){
    float proximity = dist(location.x, location.y, width/2, height/2);
    
    return proximity;
  }

}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "videoSound2o2o2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
