package com.tum.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class A {

  List<String> list =
      new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m"));

  protected void first() {
    System.out.println("Method first");
    List<String> upperCaseList = second();
    System.out.println(upperCaseList.toString());
  }

  private List<String> second() {
    return list.stream().map(this::upperCase).collect(Collectors.toList());
  }

  private String upperCase(String s) {
    save(s);
    return s.toUpperCase();
  }

  private void save(String s) {
    System.out.println("Method save: " + s);
  }
}
