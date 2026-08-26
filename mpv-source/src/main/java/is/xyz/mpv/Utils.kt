package `is`.xyz.mpv

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs

/**
 * Small source-compatible subset of the upstream mpv-android utility object.
 * The store flavor uses this instead of importing the prebuilt MPV AAR's Utils class.
 */
object Utils {
    private const val TAG = "mpv"

    fun copyAssets(context: Context) {
        val assetManager = context.assets
        val files = arrayOf("subfont.ttf", "cacert.pem")
        for (filename in files) {
            var input: java.io.InputStream? = null
            var output: java.io.OutputStream? = null
            try {
                input = assetManager.open(filename, AssetManager.ACCESS_STREAMING)
                val outputFile = File(context.filesDir, filename)
                if (outputFile.length() == input.available().toLong()) continue
                output = FileOutputStream(outputFile)
                input.copyTo(output)
            } catch (error: IOException) {
                Log.e(TAG, "Failed to copy asset file: $filename", error)
            } finally {
                try {
                    input?.close()
                    output?.close()
                } catch (_: IOException) {
                    // Asset cleanup is best effort.
                }
            }
        }
    }

    fun findRealPath(fd: Int): String? {
        var input: FileInputStream? = null
        return try {
            val path = File("/proc/self/fd/$fd").canonicalPath
            if (!path.startsWith("/proc") && File(path).canRead()) {
                input = FileInputStream(path)
                input.read()
                path
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            try {
                input?.close()
            } catch (_: IOException) {
                // Descriptor probing cleanup is best effort.
            }
        }
    }

    fun prettyTime(seconds: Int, sign: Boolean = false): String {
        if (sign) return (if (seconds >= 0) "+" else "-") + prettyTime(abs(seconds))
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        val remainingSeconds = seconds % 60
        return if (hours == 0) {
            "%02d:%02d".format(minutes, remainingSeconds)
        } else {
            "%d:%02d:%02d".format(hours, minutes, remainingSeconds)
        }
    }

    inline fun <reified T : Parcelable> getParcelableArray(bundle: Bundle, key: String): Array<T> {
        val values: Array<out Parcelable>? = if (Build.VERSION.SDK_INT >= 33) {
            bundle.getParcelableArray(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelableArray(key)
        }
        return values?.mapNotNull { it as? T }?.toTypedArray() ?: emptyArray()
    }

    val PROTOCOLS = setOf(
        "file", "content", "http", "https", "data",
        "rtmp", "rtmps", "rtp", "rtsp", "mms", "mmst", "mmsh", "tcp", "udp", "lavf"
    )

    data class Versions(
        val mpv: String,
        val buildDate: String,
        val libPlacebo: String,
        val ffmpeg: String,
    )

    val VERSIONS = Versions(
        mpv = "%MPV_VERSION%",
        buildDate = "%DATE%",
        libPlacebo = "%LIBPLACEBO_VERSION%",
        ffmpeg = "%FFMPEG_VERSION%",
    )
}
