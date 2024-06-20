package com.tum.loop;

public class B {
  protected void third() {
    System.out.println("Method third");
    for (int i = 0; i < 10; i++) {
      callSave();
    }
  }

  protected void callSave() {
    System.out.println("Method callSave");
  }
}
