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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Annotation processor to generate FlashBus event bus class.
 *
 * @author msagi (miklos.sagi@gmail.com)
 */
@SupportedAnnotationTypes("com.msagi.flashbus.annotation.Subscribe")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FlashBusGenerator extends AbstractProcessor {

    /**
     * Tag for logging.
     */
    private static final String LOG_TAG = "FlashBusGenerator";

    /**
     * The package name of the custom generated event bus class.
     */
    private static final java.lang.String EVENT_BUS_PACKAGE = "com.msagi.flashbus";

    /**
     * The simple class name of the custom generated event bus class.
     */
    private static final java.lang.String EVENT_BUS_CLASS = "EventBus";

    private static final String EVENT_BUS_CLASS_TEMPLATE = "/com/msagi/flashbus/EventBus.java.template";

    private final List<Subscriber> subscriberList = new ArrayList<>();

    private int roundIndex;

    /**
     * Print log message to diagnostic log.
     *
     * @param message The message to print.
     */
    private void log(final String message) {
        log(Diagnostic.Kind.NOTE, message, /* throwable */ null);
    }

    /**
     * Print error message to diagnostic log.
     *
     * @param message   The message to print.
     * @param throwable The error to print.
     */
    private void logError(final String message, final Throwable throwable) {
        log(Diagnostic.Kind.ERROR, message, throwable);
    }

    /**
     * Print message with optional exception to diagnostic log.
     *
     * @param messageKind The kind of the message (note, error, etc.)
     * @param message     The message to print.
     * @param throwable   The error to print (optional).
     */
    private void log(final Diagnostic.Kind messageKind, final String message, final Throwable throwable) {
        final Messager messager = processingEnv.getMessager();
        final StringWriter messageWriter = new StringWriter();
        messageWriter.append(LOG_TAG).append(": ").append(message);
        if (throwable != null) {
            messageWriter.append("\n");
            throwable.printStackTrace(new PrintWriter(messageWriter));
        }
        messager.printMessage(messageKind, messageWriter.toString());
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        log("init: start");

        final Map<String, String> options = processingEnv.getOptions();
        final Set<String> optionKeys = options.keySet();
        for (final String optionKey : optionKeys) {
            log("init: option: key: " + optionKey + ", value: " + options.get(optionKey));
        }

        log("init: done");
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        log("generate: start (round: " + roundIndex + ")");

        try {
            final String subscribeAnnotationClass = Subscribe.class.getName();
            for (final TypeElement annotation : annotations) {
                final String annotationClass = annotation.toString();
                if (subscribeAnnotationClass.equals(annotationClass)) {
                    for (final Element element : roundEnv.getElementsAnnotatedWith(annotation)) {

                        try {
                            final Subscriber subscriber = Subscriber.fromElement(element);
                            log("generate: detected: " + subscriber.toString());
                            subscriberList.add(subscriber);
                        } catch (RuntimeException rte) {
                            logError("generate: error processing subscriber", rte);
                        }
                    }
                } else {
                    logError("generate: annotation not supported: " + annotationClass, /* throwable */ null);
                }
            }

            if (roundIndex == 0) {
                generateEventBusClass();
            }
        } catch (RuntimeException rte) {
            logError("generate: runtime error", rte);
        }

        log("generate: done (round: " + roundIndex + ")");

        roundIndex++;
        return true;
    }

    /**
     * Load the event bus java class template content.
     *
     * @return The template content.
     * @throws Exception If error happens during loading.
     */
    private String loadTemplate() throws Exception {

        log("loadTemplate: start (template: " + EVENT_BUS_CLASS_TEMPLATE + ")");

        //this is a workaround of a bug (the URLConnection is caching the jar file; the other solution would be to load the Event Bus template file manually)
        new URL("http://localhost/").openConnection().setDefaultUseCaches(false);
        log("loadTemplate: java.net.URLConnection cache bug workaround applied");

        final InputStream inputStream = getClass().getResourceAsStream(EVENT_BUS_CLASS_TEMPLATE);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        inputStream.close();

        log("loadTemplate: done");

        return builder.toString();
    }

    /**
     * Generate custom event bus class.
     */
    private void generateEventBusClass() {
        final String eventBusClassName = EVENT_BUS_PACKAGE + "." + EVENT_BUS_CLASS;
        log("generateEventBusClass: start (class: " + eventBusClassName + ")");

        PrintWriter classWriter = null;
        JavaFileObject eventBusClass = null;
        try {
            final String eventBusCode = new EventBusBuilder()
                    .withSubscribers(subscriberList)
                    .withTemplate(loadTemplate())
                    .build();

            eventBusClass = processingEnv.getFiler().createSourceFile(eventBusClassName);
            classWriter = new PrintWriter(eventBusClass.openWriter());
            classWriter.write(eventBusCode);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (classWriter != null) {
                try {
                    classWriter.close();
                    log("generateEventBusClass: event bus class generated (class: " + eventBusClass.getName() + ")");
                } catch (Exception ioe) {
                    logError("generateEventBusClass: I/O error writing event bus class (class: " + eventBusClass.getName() + ")", ioe);
                }
            }
        }

        log("generateEventBusClass: done");
    }
}
