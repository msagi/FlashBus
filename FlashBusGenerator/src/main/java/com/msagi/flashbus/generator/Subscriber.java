/*
 * Copyright 2015 Miklos Sagi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msagi.flashbus.generator;

import com.msagi.flashbus.annotation.Subscribe;
import com.msagi.flashbus.annotation.ThreadId;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Data class for subscriber annotation processing.
 *
 * @author msagi (miklos.sagi@gmail.com)
 */
public class Subscriber {

    /**
     * The list of event classes processed so far.
     */
    private static final List<String> sEventClasses = new ArrayList<>();

    /**
     * The counter for unique id generator for the subscriber instances.
     */
    private static int sUidCounter = 0;

    /**
     * The unique id of the current subscriber instance.
     */
    private final int uid = sUidCounter++;

    /**
     * The class containing the subscriber method.
     */
    private final String subscriberClass;

    /**
     * The subscriber method.
     */
    private final String method;

    /**
     * The class of the event in the subscriber method.
     */
    private final String eventClass;

    /**
     * The thread id of the thread the event is to be delivered on.
     */
    private final ThreadId threadId;

    /**
     * Create new instance.
     *
     * @param subscriberClass The class containing the subscriber method.
     * @param method          The subscriber method.
     * @param eventClass      The class of the event in the subscriber method.
     * @param threadId        The thread id of the thread the event is to be delivered on.
     */
    private Subscriber(final String subscriberClass, final String method, final String eventClass, final ThreadId threadId) {
        if (subscriberClass == null) {
            throw new IllegalArgumentException("subscriberClass == null");
        }
        this.subscriberClass = subscriberClass;

        if (method == null) {
            throw new IllegalArgumentException("method == null");
        }
        this.method = method;

        if (eventClass == null) {
            throw new IllegalArgumentException("eventClass == null");
        }
        this.eventClass = eventClass;

        if (!sEventClasses.contains(eventClass)) {
            sEventClasses.add(eventClass);
        }

        if (threadId == null) {
            this.threadId = ThreadId.MAIN;
        } else {
            this.threadId = threadId;
        }
    }

    /**
     * Create new subscriber from element.
     *
     * @param element Element to fill the subscriber data from.
     * @return The subscriber instance set up from the given element.
     */
    public static Subscriber fromElement(final Element element) {
        if (!(element instanceof ExecutableElement) || ElementKind.METHOD != element.getKind()) {
            throw new IllegalArgumentException("Illegal use of @Subscribe annotation: ignored: " + element);
        }

        final ExecutableElement executableElement = (ExecutableElement) element;

        if (!executableElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new IllegalArgumentException("Illegal use of @Subscribe annotation: subscriber method must be 'public': ignored: " + element);
        }
        if (executableElement.getModifiers().contains(Modifier.STATIC)) {
            throw new IllegalArgumentException("Illegal use of @Subscribe annotation: subscriber method cannot be 'static': ignored: " + element);
        }

        final List<? extends VariableElement> variableElements = executableElement.getParameters();
        if (variableElements.size() != 1) {
            throw new IllegalArgumentException("Illegal use of @Subscribe annotation: subscriber method must have exactly one parameter: ignored: " + element);
        }

        final Element subscriberClassElement = element.getEnclosingElement();
        final Name methodName = executableElement.getSimpleName();
        final TypeMirror eventClass = variableElements.get(0).asType();
        final ThreadId threadId = element.getAnnotation(Subscribe.class).thread();

        return new Subscriber(subscriberClassElement.toString(), methodName.toString(), eventClass.toString(), threadId);
    }

    public int getEventClassId() {
        return getEventClassIdByEventClass(eventClass);
    }

    public String getSubscriberClass() {
        return subscriberClass;
    }

    public String getEventClass() {
        return eventClass;
    }

    public ThreadId getThreadId() {
        return threadId;
    }

    public String getMethod() {
        return method;
    }

    public int getUid() {
        return uid;
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Subscriber) {
            final Subscriber subscriber = (Subscriber) obj;
            return subscriber.getUid() == uid;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Subscriber[class: %s, method: %s, eventClass: %s, thread: %s]", subscriberClass, method, eventClass, threadId);
    }

    /**
     * Get the event class id for the given event class.
     *
     * @param eventClass The event class to get the id for.
     * @return The unique event class id.
     */
    public static int getEventClassIdByEventClass(final String eventClass) {
        final int eventClassId = sEventClasses.indexOf(eventClass);
        if (-1 == eventClassId) {
            throw new IllegalArgumentException("Unknown event class: " + eventClass);
        }
        return eventClassId;
    }
}
