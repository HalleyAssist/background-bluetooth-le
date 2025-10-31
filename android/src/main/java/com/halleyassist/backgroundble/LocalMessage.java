package com.halleyassist.backgroundble;

import androidx.annotation.NonNull;

public class LocalMessage {

  public enum Type {
    Service,
    Device,
  }

  public Type type;
  public String content;

  public LocalMessage(Type type, String content) {
    this.type = type;
    this.content = content;
  }

  @NonNull
  public String toString() {
    return "Type: " + type + ", Content: " + content;
  }
}
