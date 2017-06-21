#include <Servo.h>

Servo servo;
boolean isLocked = true;

void setup() {
  Serial.begin(115200);
  servo.attach(3);
  servo.write(90);
}

void loop() {
  if (Serial.available() > 0) {
    Serial.read();
    servo.write(isLocked ? 0 : 90);
    isLocked = !isLocked;
    delay(500);
  }
}

