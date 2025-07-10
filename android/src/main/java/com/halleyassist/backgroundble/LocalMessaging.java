package com.halleyassist.backgroundble;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class LocalMessaging {

    private static final Subject<String> subject = PublishSubject.create();

    public static void sendMessage(String message) {
        subject.onNext(message);
    }

    public static Subject<String> getSubject() {
        return subject;
    }
}
