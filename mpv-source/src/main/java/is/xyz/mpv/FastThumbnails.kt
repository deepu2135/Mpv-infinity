package `is`.xyz.mpv

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Source-build replacement for the unpublished FastThumbnails JNI extension.
 *
 * The Standard flavor retains its existing native FastThumbnails implementation. The isolated
 * store flavor uses the platform retriever so thumbnail generation remains available without
 * shipping the untraceable prebuilt extension.
 */
object FastThumbnails {
  @Volatile
  private var appContext: Context? = null

  @JvmStatic
  fun initialize(context: Context) {
    appContext = context.applicationContext
  }

  @JvmStatic
  suspend fun generateAsync(
    path: String,
    positionSeconds: Double,
    maxDimension: Int,
    useHwDec: Boolean = false,
  ): Bitmap? = withContext(Dispatchers.IO) {
    val context = appContext ?: return@withContext null
    if (path.isBlank() || maxDimension <= 0) return@withContext null

    val retriever = MediaMetadataRetriever()
    try {
      if (path.startsWith("content://")) {
        retriever.setDataSource(context, Uri.parse(path))
      } else {
        retriever.setDataSource(path)
      }

      val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
      val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
      val scale = maxDimension.toFloat() / max(1, max(width, height))
      val targetWidth = max(1, (width * scale).toInt())
      val targetHeight = max(1, (height * scale).toInt())
      val timeUs = (positionSeconds.coerceAtLeast(0.0) * 1_000_000.0).toLong()

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
        retriever.getScaledFrameAtTime(
          timeUs,
          MediaMetadataRetriever.OPTION_CLOSEST,
          targetWidth,
          targetHeight,
        )
      } else {
        retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
      }
    } finally {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) retriever.close() else retriever.release()
    }
  }

  @JvmStatic
  fun clearThumbnailCache() = Unit
}
