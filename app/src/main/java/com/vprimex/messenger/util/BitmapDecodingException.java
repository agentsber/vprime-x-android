package com.vprimex.messenger.util;

public class BitmapDecodingException extends Exception {

  public BitmapDecodingException(String s) {
    super(s);
  }

  public BitmapDecodingException(Exception nested) {
    super(nested);
  }
}
