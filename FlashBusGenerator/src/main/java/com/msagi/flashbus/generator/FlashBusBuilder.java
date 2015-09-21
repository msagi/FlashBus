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

import com.msagi.flashbus.annotation.ThreadId;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Builder for the custom generated FlashBus event bus class.
 *
 * @author msagi (miklos.sagi@gmail.com)
 */
public class FlashBusBuilder {

    //makers in the template class which will be replaced by the generated code
    private static final String MARKER_DEBUG = "{Debug}";

    private static final String MARKER_PACKAGE = "{Package}";

    private static final String MARKER_IMPORTS = "{Imports}";

    private static final String MARKER_INNER_CLASSES = "{InnerClasses}";

    private static final String MARKER_FIELDS = "{Fields}";

    private static final String MARKER_METHODS = "{Methods}";

    private StringBuilder logBuilder;

    private StringBuilder codeBuilderForPackage;
    private StringBuilder codeBuilderForSubscriberClassImports;
    private StringBuilder codeBuilderForEventClassImports;
    private StringBuilder codeBuilderForInnerClasses;
    private StringBuilder codeBuilderForFields;
    private StringBuilder codeBuilderForMethods;

    private Hashtable<String, ArrayList<Subscriber>> subscribersBySubscriberClass;
    private Hashtable<String, ArrayList<Subscriber>> subscribersByEventClass;

    /**
     * The unique event classes in the subscriber list.
     */
    private List<String> eventClasses;

    /**
     * The package name of the generated event bus class.
     */
    private String packageName;

    /**
     * The debug flag of the generated event bus.
     */
    private Boolean debug = false;

    /**
     * The list of subscribers to build the event bus for.
     */
    private List<Subscriber> subscriberList;

    /**
     * The template to use as a basis of the generated event bus class.
     */
    private String template;

    /**
     * Set event bus debug flag.
     * @param debug The debug flag to build the event bus with.
     * @return The builder instance to support chaining.
     */
    public FlashBusBuilder withDebug(final boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Set event bus package name.
     * @param packageName The package name to use as event bus package.
     * @return The builder instance to support chaining.
     */
    public FlashBusBuilder withPackage(final String packageName) {
        this.packageName = packageName;
        return this;
    }

    /**
     * Set given subscriber list in the state of the builder.
     *
     * @param subscriberList The list of subscribers to generate code to.
     * @return The builder instance to support chaining.
     */
    public FlashBusBuilder withSubscribers(final List<Subscriber> subscriberList) {
        if (subscriberList == null) {
            throw new IllegalArgumentException("subscriberList == null");
        }
        this.subscriberList = subscriberList;
        return this;
    }

    /**
     * The template to use as a basis of the generated event bus class.
     *
     * @param template The template class content.
     * @return The builder instance to support chaining.
     */
    public FlashBusBuilder withTemplate(final String template) {
        if (template == null) {
            throw new IllegalArgumentException("template == null");
        }
        this.template = template;
        return this;
    }


    /**
     * Pre-process subscribers and build internal 'subscribers by subscriber class' and 'subscribers by event class' mapping tables.
     * These tables will be used to generate the subscriber class and event class related code segments of the custom generated event bus.
     */
    private void preProcessSubscribers() {

        subscribersBySubscriberClass = new Hashtable<>();
        subscribersByEventClass = new Hashtable<>();

        int subscriberUid = 0;
        eventClasses = new ArrayList<>();

        for (final Subscriber subscriber : subscriberList) {

            subscriber.setUid(subscriberUid++);

            final String subscriberClass = subscriber.getSubscriberClass();
            final String eventClass = subscriber.getEventClass();
            final String method = subscriber.getMethod();

            if (!eventClasses.contains(eventClass)) {
                eventClasses.add(eventClass);
            }
            subscriber.setEventClassId(eventClasses.indexOf(eventClass));

            logBuilder
                    .append("Analyzing subscriber (subscriber: ").append(subscriberClass).append(" event:").append(eventClass).append(" method:").append(method)
                    .append(")\n");

            //map subscriber class to subscribers
            final ArrayList<Subscriber> subscriberListBySubscriberClass;
            if (subscribersBySubscriberClass.get(subscriberClass) != null) {
                subscriberListBySubscriberClass = subscribersBySubscriberClass.get(subscriberClass);
            } else {
                subscriberListBySubscriberClass = new ArrayList<>();
                subscribersBySubscriberClass.put(subscriberClass, subscriberListBySubscriberClass);
                logBuilder.append("New subscriber class list created (subscriber: ").append(subscriberClass).append("\n");
            }
            if (!subscriberListBySubscriberClass.contains(subscriber)) {
                subscriberListBySubscriberClass.add(subscriber);

                logBuilder
                        .append("Subscriber added to 'subscriber list by ").append(subscriberClass).append(" subscriber class' list (subscriber:").append(subscriberClass)
                        .append(" event:").append(eventClass).append(" method:").append(method).append(")\n");
            }

            //map event class to subscribers
            final ArrayList<Subscriber> subscriberListByEventClass;
            if (subscribersByEventClass.get(eventClass) != null) {
                subscriberListByEventClass = subscribersByEventClass.get(eventClass);
            } else {
                subscriberListByEventClass = new ArrayList<>();
                subscribersByEventClass.put(eventClass, subscriberListByEventClass);
                logBuilder
                        .append("New event class list created (event: ").append(eventClass).append("\n");
            }
            if (!subscriberListByEventClass.contains(subscriber)) {
                subscriberListByEventClass.add(subscriber);

                logBuilder
                        .append("Subscriber added to 'subscriber list by ").append(eventClass).append(" event class' list (subscriber:").append(subscriberClass)
                        .append(" event:").append(eventClass).append(" method:").append(method).append(")\n");
            }
        }
    }

    /**
     * Generate subscriber class related code segments of the custom generated event bus (import for subscriber class, list of registered subscriber class instances,
     * 'register', 'unregister' methods.
     */
    private void generateSubscriberClassRelatedCode() {
        final Enumeration<String> subscriberClasses = subscribersBySubscriberClass.keys();
        int subscriberClassId = 0;
        while (subscriberClasses.hasMoreElements()) {
            final String subscriberClass = subscriberClasses.nextElement();
            final ArrayList<Subscriber> subscribers = subscribersBySubscriberClass.get(subscriberClass);

            codeBuilderForSubscriberClassImports.append("import ").append(subscriberClass).append(";\n");

            //generate 'list of registered subscriber instances' for subscriber class
            final String registeredSubscriberListName = "mRegisteredSubscribers" + subscriberClassId;
            codeBuilderForFields
                    .append("private final ArrayList<").append(subscriberClass).append("> ").append(registeredSubscriberListName).append(" = new ArrayList<>();\n");

            // generate 'register' methods
            logBuilder
                    .append("Generating 'register' for ").append(subscriberClass).append("\n");

            codeBuilderForMethods
                    .append("public final void register(final ").append(subscriberClass).append(" subscriber) {\n")
                    .append("\tif (subscriber == null) { return; }\n")
                    .append("\tsynchronized(this) {\n")
                    .append("\t\tif (").append(registeredSubscriberListName).append(".contains(subscriber)) { return; }\n")
                    .append("\t\t").append(registeredSubscriberListName).append(".add(subscriber);\n");

            for (final Subscriber subscriber : subscribers) {

                final int dispatcherUid = subscriber.getUid();
                final String dispatcherClassName = "Dispatcher" + dispatcherUid;
                final String dispatcherVariableName = "dispatcher" + dispatcherUid;
                final String eventClass = subscriber.getEventClass();
                final int eventClassId = subscriber.getEventClassId();

                codeBuilderForMethods
                        .append("\t\tfinal ").append(dispatcherClassName).append(" ").append(dispatcherVariableName).append(" = new ").append(dispatcherClassName)
                        .append("(subscriber, ");

                if (ThreadId.MAIN == subscriber.getThreadId()) {
                    codeBuilderForMethods.append("MAIN_HANDLER");
                } else {
                    codeBuilderForMethods.append("BACKGROUND_HANDLER");
                }

                final String stickyEventVariableName = "stickyEvent" + dispatcherUid;

                codeBuilderForMethods
                        .append(");\n")
                        .append("\t\tfinal ").append(eventClass).append(" ").append(stickyEventVariableName).append(" = getStickyEvent(").append(eventClass)
                        .append(".class);\n")
                        .append("\t\tif (").append(stickyEventVariableName).append(" != null) { ").append(dispatcherVariableName).append(".dispatch(")
                        .append(stickyEventVariableName).append("); }\n")
                        .append("\t\tmDispatcherList").append(eventClassId).append(".add(").append(dispatcherVariableName).append(");\n");

            }

            codeBuilderForMethods
                    .append("\t}\n")
                    .append("}\n\n");

            // generate 'unregister' methods
            logBuilder
                    .append("Generating 'unregister' for ").append(subscriberClass).append("\n");

            codeBuilderForMethods
                    .append("public final void unregister(final ").append(subscriberClass).append(" subscriber) {\n")
                    .append("\tif (subscriber == null) { return; }\n")
                    .append("\tsynchronized(this) {\n")
                    .append("\t\tif (!").append(registeredSubscriberListName).append(".contains(subscriber)) { return; }\n")
                    .append("\t\t").append(registeredSubscriberListName).append(".remove(subscriber);\n");

            for (final Subscriber subscriber : subscribers) {

                final int eventClassId = subscriber.getEventClassId();
                final String dispatcherListName = "mDispatcherList" + eventClassId;
                final String dispatcherListIteratorName = "dispatcherList" + eventClassId + "Iterator";

                codeBuilderForMethods
                        .append("\t\tfinal Iterator<Dispatcher> ").append(dispatcherListIteratorName).append(" = ").append(dispatcherListName).append(".iterator();\n")
                        .append("\t\twhile (").append(dispatcherListIteratorName).append(".hasNext()) {\n")
                        .append("\t\t\tif (").append(dispatcherListIteratorName).append(".next().mSubscriber == subscriber) {\n")
                        .append("\t\t\t\t").append(dispatcherListIteratorName).append(".remove();\n")
                        //no 'break;' here since a subscriber can listen to the same event multiple times
                        .append("\t\t\t}\n")
                        .append("\t\t}\n");

            }

            codeBuilderForMethods
                    .append("\t}\n")
                    .append("}\n\n");

            subscriberClassId++;
        }
    }

    /**
     * Generate event class related code segments of the custom generated event bus (import for event class, list of dispatcher class instances,
     * 'post', 'postSticky', dispatcher class and list of registered dispatcher instances.
     */
    private void generateEventClassRelatedCode() {

        final Enumeration<String> eventClasses = subscribersByEventClass.keys();
        while (eventClasses.hasMoreElements()) {
            final String eventClass = eventClasses.nextElement();
            final int eventClassId = this.eventClasses.indexOf(eventClass);
            final ArrayList<Subscriber> subscribers = subscribersByEventClass.get(eventClass);

            logBuilder
                    .append("Number of subscribers by event ").append(eventClass).append(": ").append(subscribers.size()).append("\n");

            //import event class
            codeBuilderForEventClassImports.append("import ").append(eventClass).append(";\n");

            //generate 'list of dispatchers' field for event class
            final String dispatcherListName = "mDispatcherList" + eventClassId;
            codeBuilderForFields
                    .append("private final ArrayList<Dispatcher> ").append(dispatcherListName).append(" = new ArrayList<>(EVENT_DISPATCHER_LIST_INITIAL_CAPACITY);\n");

            logBuilder
                    .append("Generating 'post' for event ").append(eventClass).append("\n");

            //generate 'post method' for event class
            codeBuilderForMethods
                    .append("public final void post(final ").append(eventClass).append(" event) {\n")
                    //TODO if we keep the list size separately then this method is not thread safe: HOWTO sync this without loosing performance?
                    // e.g. when .unregister is called from a separate thread this could cause NPE with list.get(...).dispatch(...)
                    .append("\tfinal int dispatcherListSize = ").append(dispatcherListName).append(".size();\n")
                    .append("\tfor (int index = 0; index < dispatcherListSize; index++) {\n")
                    .append("\t\t").append(dispatcherListName).append(".get(index).dispatch(event);\n")
                    .append("\t}\n")
                    .append("}\n\n");

            //generate 'post sticky method' for event class
            codeBuilderForMethods
                    .append("public final void postSticky(final ").append(eventClass).append(" event) {\n")
                    .append("\tif (event == null) { return; }\n")
                    .append("\tmStickyEvents.put(event.getClass(), event);\n")
                            //TODO msagi: unrolling this call would increase speed on background thread with multi core CPUs if event delivery order is not important
                    .append("\tpost(event);\n")
                    .append("}\n\n");

            for (final Subscriber subscriber : subscribers) {

                final String subscriberClass = subscriber.getSubscriberClass();
                final String subscriberMethod = subscriber.getMethod();

                //generate dispatcher for each event handler method of each event subscriber classes

                final String dispatcherClassName = "Dispatcher" + subscriber.getUid();

                logBuilder
                        .append("Generating Dispatcher (").append(dispatcherClassName).append(" for subscriber ").append(subscriberClass).append(", event ")
                        .append(eventClass).append("\n");

                codeBuilderForInnerClasses
                        .append("private static final class ").append(dispatcherClassName).append(" extends Dispatcher<").append(subscriberClass).append(", ")
                        .append(eventClass).append("> {\n")
                        .append("\n")
                        .append("\tpublic ").append(dispatcherClassName).append("(final ").append(subscriberClass).append(" subscriber, final Handler handler) {\n")
                        .append("\t\tsuper(subscriber, handler);\n")
                        .append("\t}\n")
                        .append("\n")
                        .append("\t@Override\n")
                        .append("\tpublic void run() {\n")
                        .append("\t\t").append(eventClass).append(" event;\n")
                        .append("\t\twhile ((event = mEventQueue.poll()) != null) {\n")
                        .append("\t\t\ttry {\n")
                        .append("\t\t\t\tmSubscriber.").append(subscriberMethod).append("(event);\n")
                        .append("\t\t\t} catch (RuntimeException re) {\n")
                        .append("\t\t\t\tLog.e(TAG, \"Error dispatching event\", re);\n")
                        .append("\t\t\t}\n")
                        .append("\t\t}\n")
                        .append("\t\tmIsDispatchingActive.set(false);\n")
                        .append("\t}\n")
                        .append("}\n\n");
            }
        }
    }

    /**
     * Generate code.
     *
     * @return The generated code.
     */
    public String build() {
        if (template == null || subscriberList == null) {
            throw new IllegalStateException("Invalid builder state");
        }

        logBuilder = new StringBuilder();

        codeBuilderForPackage = new StringBuilder();
        if (packageName != null) {
            codeBuilderForPackage.append("package ").append(packageName).append(";\n");
        }

        codeBuilderForSubscriberClassImports = new StringBuilder();
        codeBuilderForEventClassImports = new StringBuilder();
        codeBuilderForInnerClasses = new StringBuilder();
        codeBuilderForFields = new StringBuilder();
        codeBuilderForMethods = new StringBuilder();

        logBuilder
                .append("Generating event bus...\n")
                .append("Total subscribers: ").append(subscriberList.size()).append("\n");

        preProcessSubscribers();

        generateSubscriberClassRelatedCode();

        generateEventClassRelatedCode();

        return template
                .replace(MARKER_DEBUG, debug.toString())
                .replace(MARKER_PACKAGE, codeBuilderForPackage)
                .replace(MARKER_IMPORTS, codeBuilderForSubscriberClassImports.toString() + "\n" + codeBuilderForEventClassImports.toString())
                .replace(MARKER_INNER_CLASSES, codeBuilderForInnerClasses.toString())
                .replace(MARKER_FIELDS, codeBuilderForFields.toString())
                .replace(MARKER_METHODS, codeBuilderForMethods.toString())
                .concat("\n\n/*\n" + logBuilder.toString() + "*/");
    }
}
