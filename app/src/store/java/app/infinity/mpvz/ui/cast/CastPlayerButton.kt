package app.infinity.mpvz.ui.cast

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * Store flavor intentionally omits Google Cast/GMS. The Cast control is unavailable in this
 * distribution, while the Standard, noVulkan, and fongmi flavors retain their original button.
 */
@Composable
fun CastPlayerButton(
    hideBackground: Boolean,
    buttonSize: Dp,
) {
    // Intentionally empty: Cast is not part of the store distribution.
}
