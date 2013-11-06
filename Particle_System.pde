class Particle {
  PVector location, velocity, acceleration, goingTowards, origin, face;
  float limit, diameter, opacity, wiggle, modAcc;
  boolean stopAcc = false;
  color couleur;

  Particle(PVector l, float diam, color col) {
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
  
  void setOrigin(int num){
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

  void run() {
    update();
    displayColor();
  }
  
  // Method to update location
  void update() {
    PVector dir = PVector.sub(goingTowards,location);

    dir.normalize();
    dir.mult(wiggle);
    acceleration = dir;
    
    velocity.add(acceleration);
    velocity.limit(limit);
    location.add(velocity);
  }
  
  void goBack(){
   goingTowards = origin; 
  }
  
  void goToFace(){
   goingTowards = face; 
  }
  
  void goToCenter(){
     goingTowards = new PVector(width/2, height/2); 
  }
  
  void goToRandom(){
    float randomX = random(-width, width*2);
    float randomY = random(-height, height*2);
    goingTowards = new PVector(randomX, randomY); 
  }
  
  void setWiggle(float wiggleIn){
     wiggle = wiggleIn;
  }
  
  float getWiggle(){
    return wiggle; 
  }

  void setColor(color couleurIn){
    couleur = couleurIn;
  }
  
  void setLimit(float limitIn){
     limit = limitIn; 
  }
  
  void modAcc(float accIn){
     modAcc = accIn;
  }
  
  PVector getLoc(){
     return location; 
  }
  
  void display() {
    fill(norm(location.x, 0, width)*250,norm(location.x, 0, width)*80,norm(location.y, 0, height)*200,100);
    ellipse(location.x,location.y,diameter,diameter);
  }
  
  void displayColor() {
    fill(couleur,opacity);
    ellipse(location.x,location.y,diameter,diameter);
  }
  
  void mouseProx(){
     PVector mouse = new PVector(mouseX, mouseY); 
     PVector proximity = PVector.sub(location, mouse);
     diameter = width/2 - proximity.x;
  }
  
  PVector getPos(){
     return location; 
  }
  
  float EdgeProx(){
    float proximity = dist(location.x, location.y, width/2, height/2);
    
    return proximity;
  }

}
