package com.tum.stream;

public class Main {

  /**
   * This package will be used to test method calls in loops. Method calls that were not written by
   * the developer are ignored. <br>
   * <br>
   *
   * <p>A.first() will call the methods in the following order:
   *
   * <ul>
   *   <li>A.first() -> A.second() -> A.upperCase() -> A.save()
   * </ul>
   *
   * <p>A.second() will call the methods in the following order in a stream loop:
   *
   * <ul>
   *   <li>A.second() -> A.upperCase() -> A.save()
   * </ul>
   *
   * <p>B.third() will call the methods in the following order:
   *
   * <ul>
   *   <li>A.upperCase() -> A.save()
   * </ul>
   *
   * <p>Main.main() will call the methods in the following order:
   *
   * <ul>
   *   <li>A.first() -> A.second() -> A.upperCase() -> A.save()
   * </ul>
   */
  public static void main(String[] args) {
    A a = new A();
    a.first();
  }
}
