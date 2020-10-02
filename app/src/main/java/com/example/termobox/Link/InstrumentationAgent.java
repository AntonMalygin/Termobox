package com.example.termobox.Link;

import android.app.Instrumentation;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;


public class InstrumentationAgent {
    private static volatile Instrumentation globalInstrumentation;

    public static void premain(final String agentArgs, final Instrumentation inst) {
        globalInstrumentation = inst;
    }

    public static long getObjectSize(final Object object) {
        if (globalInstrumentation == null) {
            throw new IllegalStateException("Agent not initialized.");
        }
        return globalInstrumentation.getObjectSize(object);
    }
}
