package com.tum.loop;

public class A {
  protected void first() {
    System.out.println("Method first");
    second();
  }

  private void second() {
    System.out.println("Method second");
    B b = new B();
    b.third();
  }
}
