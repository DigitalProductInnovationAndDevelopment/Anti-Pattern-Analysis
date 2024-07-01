package com.tum.loop;

public class Main {

  /**
   * This package will be used to test method calls in loops. <br>
   * <br>
   *
   * <p>A.first() will call the methods in the following order:
   *
   * <ul>
   *   <li>A.first() -> A.second() -> B.third() -> B.callSave()
   * </ul>
   *
   * <p>A.second() will call the methods in the following order:
   *
   * <ul>
   *   <li>A.second() -> B.third() -> B.callSave()
   * </ul>
   *
   * <p>B.third() will call the methods in the following order:
   *
   * <ul>
   *   <li>B.third() -> B.callSave()
   * </ul>
   *
   * <p>Main.main() will call the methods in the following order:
   *
   * <ul>
   *   <li>A.first() -> A.second() -> B.third() -> B.callSave()
   * </ul>
   */
  public static void main(String[] args) {
    A a = new A();
    a.first();
  }
}
