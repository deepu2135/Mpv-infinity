package is.xyz.mpv;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Static libmpv facade used by Mpv∞.
 *
 * This is intentionally kept source-compatible with the existing MPV AAR. The native lifecycle is
 * create -> option writes -> init, which is required by PlaybackSession for config-dir and profile
 * setup. The Standard flavor continues to use its existing AAR; this class is compiled only into
 * the store source-build flavor.
 */
@SuppressWarnings("unused")
public final class MPVLib {
    private static Context thumbnailContext;

    private MPVLib() {}

    static {
        String[] libs = { "mpv", "player" };
        for (String lib : libs) System.loadLibrary(lib);
    }

    public static native void create(Context appctx, String logLvl);
    public static void create(Context appctx) { create(appctx, "info"); }
    public static native void init();
    public static native void destroy();
    public static native void attachSurface(Surface surface);
    public static native void detachSurface();

    public static native void command(@NonNull String... cmd);
    public static native MPVNode commandNode(@NonNull String... cmd);

    public static native int setOptionString(@NonNull String name, @NonNull String value);
    public static native Bitmap grabThumbnail(int dimension);

    /** Source-only replacement for the historical custom thumbnail JNI entry point. */
    public static Bitmap grabThumbnailFast(String path, double position, int dimension, boolean useHwDec) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (path.startsWith("content://") && thumbnailContext != null) {
                retriever.setDataSource(thumbnailContext, Uri.parse(path));
            } else {
                retriever.setDataSource(path);
            }
            long timeUs = Math.max(0L, (long) (position * 1_000_000.0));
            Bitmap frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (frame == null || dimension <= 0) return frame;
            int width = frame.getWidth();
            int height = frame.getHeight();
            float scale = Math.min(1f, dimension / (float) Math.max(width, height));
            if (scale >= 1f) return frame;
            return Bitmap.createScaledBitmap(frame,
                    Math.max(1, Math.round(width * scale)),
                    Math.max(1, Math.round(height * scale)), true);
        } catch (RuntimeException ignored) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (IOException ignored) {
                // Thumbnail cleanup must not fail the playback-facing API.
            }
        }
    }

    public static native Integer getPropertyInt(@NonNull String property);
    public static native void setPropertyInt(@NonNull String property, @NonNull Integer value);
    public static native Long getPropertyLong(@NonNull String property);
    public static native void setPropertyLong(@NonNull String property, @NonNull Long value);
    public static native Double getPropertyDouble(@NonNull String property);
    public static native void setPropertyDouble(@NonNull String property, @NonNull Double value);
    public static native Float getPropertyFloat(@NonNull String property);
    public static native void setPropertyFloat(@NonNull String property, @NonNull Float value);
    public static native Boolean getPropertyBoolean(@NonNull String property);
    public static native void setPropertyBoolean(@NonNull String property, @NonNull Boolean value);
    public static native String getPropertyString(@NonNull String property);
    public static native void setPropertyString(@NonNull String property, @NonNull String value);
    public static native MPVNode getPropertyNode(@NonNull String property);
    public static native void setPropertyNode(@NonNull String property, @NonNull MPVNode value);
    public static native void observeProperty(@NonNull String property, int format);

    private static final List<EventObserver> observers = new ArrayList<>();
    private static final List<LogObserver> logObservers = new ArrayList<>();

    public static synchronized void addObserver(EventObserver observer) {
        if (!observers.contains(observer)) observers.add(observer);
    }
    public static synchronized void removeObserver(EventObserver observer) {
        observers.remove(observer);
    }
    public static synchronized void addLogObserver(LogObserver observer) {
        if (!logObservers.contains(observer)) logObservers.add(observer);
    }
    public static synchronized void removeLogObserver(LogObserver observer) {
        logObservers.remove(observer);
    }

    public static void eventProperty(String property) {
        for (EventObserver o : snapshotObservers()) o.eventProperty(property);
    }
    public static void eventProperty(String property, long value) {
        for (EventObserver o : snapshotObservers()) o.eventProperty(property, value);
    }
    public static void eventProperty(String property, boolean value) {
        for (EventObserver o : snapshotObservers()) o.eventProperty(property, value);
    }
    public static void eventProperty(String property, double value) {
        for (EventObserver o : snapshotObservers()) o.eventProperty(property, value);
    }
    public static void eventProperty(String property, String value) {
        for (EventObserver o : snapshotObservers()) o.eventProperty(property, value);
    }
    public static void eventProperty(String property, MPVNode value) {
        for (EventObserver o : snapshotObservers()) o.eventProperty(property, value);
    }
    public static void event(int eventId) {
        for (EventObserver o : snapshotObservers()) o.event(eventId, MPVNode.None.INSTANCE);
    }
    public static void event(int eventId, MPVNode data) {
        for (EventObserver o : snapshotObservers()) o.event(eventId, data);
    }
    public static void efEvent(String error) {
        for (EventObserver o : snapshotObservers()) o.efEvent(error);
    }
    public static void logMessage(String prefix, int level, String text) {
        List<LogObserver> copy;
        synchronized (MPVLib.class) { copy = new ArrayList<>(logObservers); }
        for (LogObserver o : copy) o.logMessage(prefix, level, text);
    }

    private static synchronized List<EventObserver> snapshotObservers() {
        return new ArrayList<>(observers);
    }

    public interface EventObserver {
        void eventProperty(@NonNull String property);
        void eventProperty(@NonNull String property, long value);
        void eventProperty(@NonNull String property, boolean value);
        void eventProperty(@NonNull String property, @NonNull String value);
        void eventProperty(@NonNull String property, double value);
        void eventProperty(@NonNull String property, @NonNull MPVNode value);
        void event(int eventId, @NonNull MPVNode data);
        void efEvent(String error);
    }

    public interface LogObserver {
        void logMessage(@NonNull String prefix, int level, @NonNull String text);
    }

    public static final class MpvFormat {
        public static final int MPV_FORMAT_NONE = 0;
        public static final int MPV_FORMAT_STRING = 1;
        public static final int MPV_FORMAT_OSD_STRING = 2;
        public static final int MPV_FORMAT_FLAG = 3;
        public static final int MPV_FORMAT_INT64 = 4;
        public static final int MPV_FORMAT_DOUBLE = 5;
        public static final int MPV_FORMAT_NODE = 6;
        public static final int MPV_FORMAT_NODE_ARRAY = 7;
        public static final int MPV_FORMAT_NODE_MAP = 8;
        public static final int MPV_FORMAT_BYTE_ARRAY = 9;
        private MpvFormat() {}
    }

    public static final class MpvEvent {
        public static final int MPV_EVENT_NONE = 0;
        public static final int MPV_EVENT_SHUTDOWN = 1;
        public static final int MPV_EVENT_LOG_MESSAGE = 2;
        public static final int MPV_EVENT_GET_PROPERTY_REPLY = 3;
        public static final int MPV_EVENT_SET_PROPERTY_REPLY = 4;
        public static final int MPV_EVENT_COMMAND_REPLY = 5;
        public static final int MPV_EVENT_START_FILE = 6;
        public static final int MPV_EVENT_END_FILE = 7;
        public static final int MPV_EVENT_FILE_LOADED = 8;
        public static final int MPV_EVENT_IDLE = 11;
        public static final int MPV_EVENT_TICK = 14;
        public static final int MPV_EVENT_CLIENT_MESSAGE = 16;
        public static final int MPV_EVENT_VIDEO_RECONFIG = 17;
        public static final int MPV_EVENT_AUDIO_RECONFIG = 18;
        public static final int MPV_EVENT_SEEK = 20;
        public static final int MPV_EVENT_PLAYBACK_RESTART = 21;
        public static final int MPV_EVENT_PROPERTY_CHANGE = 22;
        public static final int MPV_EVENT_QUEUE_OVERFLOW = 24;
        public static final int MPV_EVENT_HOOK = 25;
        private MpvEvent() {}
    }

    public static final class MpvLogLevel {
        public static final int MPV_LOG_LEVEL_NONE = 0;
        public static final int MPV_LOG_LEVEL_FATAL = 10;
        public static final int MPV_LOG_LEVEL_ERROR = 20;
        public static final int MPV_LOG_LEVEL_WARN = 30;
        public static final int MPV_LOG_LEVEL_INFO = 40;
        public static final int MPV_LOG_LEVEL_V = 50;
        public static final int MPV_LOG_LEVEL_DEBUG = 60;
        public static final int MPV_LOG_LEVEL_TRACE = 70;
        private MpvLogLevel() {}
    }

    /** Kept for source compatibility; this source path uses the process JavaVM automatically. */
    public static synchronized void setThumbnailJavaVM(Context context) {
        thumbnailContext = context == null ? null : context.getApplicationContext();
    }
}
