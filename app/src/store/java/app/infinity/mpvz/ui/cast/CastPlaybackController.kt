package app.infinity.mpvz.ui.cast

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity

/** Metadata contract retained for shared player code; Cast transport is absent in the store build. */
data class CastMediaSnapshot(
    val source: Uri,
    val title: String,
    val mimeType: String?,
    val durationMs: Long,
    val positionMs: Long,
    val isPlaying: Boolean,
)

/**
 * Store-flavor no-op controller. Keeping this narrow adapter lets shared player lifecycle code
 * remain unchanged while the store APK contains no Google Cast/GMS implementation.
 */
class CastPlaybackController(
    activity: AppCompatActivity,
    currentMedia: () -> CastMediaSnapshot?,
    pauseLocal: () -> Unit,
    restoreLocal: (positionMs: Long, play: Boolean) -> Unit,
    notifyUser: (String) -> Unit,
) {
    fun start() = Unit
    fun release() = Unit
}
