package com.halleyassist.backgroundble;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class LocalMessaging {

  private static final Subject<LocalMessage> subject = PublishSubject.create();

  public static void sendMessage(LocalMessage message) {
    subject.onNext(message);
  }

  public static Subject<LocalMessage> getSubject() {
    return subject;
  }
}
