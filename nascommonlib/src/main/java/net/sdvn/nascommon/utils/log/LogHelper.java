package net.sdvn.nascommon.utils.log;//package com.eli.oneos.model.log;
//
//import androidx.annotation.Nullable;
//import android.util.Log;
//
//import com.eli.oneos.BuildConfig;
//
//public class LogHelper {
//
//    private static final String LOG_PREFIX = "nas_";
//    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
//    private static final int MAX_LOG_TAG_LENGTH = 23;
//
//    public static String makeLogTag(String str) {
//        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
//            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
//        }
//
//        return LOG_PREFIX + str;
//    }
//
//    /**
//     * Don't use this when obfuscating class names!
//     */
//    public static String makeLogTag(Class cls) {
//        return makeLogTag(cls.getSimpleName());
//    }
//
//
//    public static void v(String tag, Object... messages) {
//        // Only log VERBOSE if build type is DEBUG
//        if (BuildConfig.DEBUG) {
//            log(tag, Log.VERBOSE, null, messages);
//        }
//    }
//
//    public static void d(Object o, Object... messages) {
//        d(makeLogTag(o.getClass()), messages);
//    }
//
//    public static void d(String tag, Object... messages) {
//        // Only log DEBUG if build type is DEBUG
//        if (BuildConfig.DEBUG) {
//            log(tag, Log.DEBUG, null, messages);
//        }
//    }
//
//    public static void i(String tag, Object... messages) {
//        if (BuildConfig.DEBUG)
//            log(tag, Log.INFO, null, messages);
//    }
//
//    public static void w(String tag, Object... messages) {
//        log(tag, Log.WARN, null, messages);
//    }
//
//    public static void w(String tag, Throwable t, Object... messages) {
//        log(tag, Log.WARN, t, messages);
//    }
//
//    public static void e(String tag, Object... messages) {
//        log(tag, Log.ERROR, null, messages);
//    }
//
//    public static void e(String tag, Throwable t, Object... messages) {
//        log(tag, Log.ERROR, t, messages);
//    }
//
//    public static void log(String tag, int level, @Nullable Throwable t, @Nullable Object... messages) {
//        if (BuildConfig.DEBUG || Log.isLoggable(tag, level)) {
//            String message;
//            if (t == null && messages != null && messages.length == 1) {
//                // handle this common case without the extra cost of creating a stringbuffer:
//                message = messages[0].toString();
//            } else {
//                StringBuilder sb = new StringBuilder();
//                if (messages != null) for (Object m : messages) {
//                    sb.append(m);
//                }
//                if (t != null) {
//                    sb.append("\n").append(Log.getStackTraceString(t));
//                }
//                message = sb.toString();
//            }
//            try {
//                Log.println(level, tag, message);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
