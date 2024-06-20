package com.tum.methodchain;

public class A {
  protected static void first() {
    System.out.println("Method first");
    B.second();
  }

  protected static void callOne() {
    System.out.println("Method callOne");
  }
}
