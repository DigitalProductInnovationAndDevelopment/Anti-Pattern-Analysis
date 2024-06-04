package com.tum.methodchain;

public class B {
  protected static void second() {
    System.out.println("Method second");
    C.third();
  }

  protected static void callTwo() {
    System.out.println("Method callTwo");
  }
}
