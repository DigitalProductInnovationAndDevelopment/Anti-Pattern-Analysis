package com.tum.methodchain;

public class Main {

  /**
   * This package will be used to test method chain catches. <br>
   * <br>
   *
   * <p>A.first() will call the methods in the following order:
   *
   * <ul>
   *   <li>A.first() -> B.second() -> C.third()
   * </ul>
   *
   * <p>B.second() will call the methods in the following order:
   *
   * <ul>
   *   <li>B.second() -> C.third()
   * </ul>
   *
   * <p>Main.main() will call the methods in the following order:
   *
   * <ul>
   *   <li>A.first() -> B.second() -> C.third()
   *   <li>B.second() -> C.third()
   *   <li>C.third()
   *   <li>A.callOne()
   *   <li>B.callTwo()
   *   <li>C.callThird()
   * </ul>
   */
  public static void main(String[] args) {
    A.first();
    B.second();
    C.third();

    System.out.println("-----------------");

    A.callOne();
    B.callTwo();
    C.callThird();
  }
}
