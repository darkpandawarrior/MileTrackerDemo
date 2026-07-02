package com.mileway

import com.mileway.core.common.deeplink.DeepLinkRouter
import com.mileway.core.common.deeplink.DeepLinkTarget
import org.junit.Test
import kotlin.test.assertFalse

/**
 * P7.2: guards `res/xml/shortcuts.xml`'s static App Shortcuts (and their App Actions `capability`)
 * against silently rotting if [DeepLinkRouter]'s recognised routes ever change — the shortcuts are
 * plain XML, so nothing else would fail to compile if one of these `mileway://` targets stopped
 * resolving. Keep this list in sync with the `android:data` values in `shortcuts.xml`.
 */
class AppShortcutsDeepLinksTest {
    @Test
    fun `static shortcut deep links resolve to known targets`() {
        listOf(
            "mileway://track" to "start_tracking (+ actions_intent_START_TRACKING capability)",
            "mileway://log/expense" to "log_expense",
        ).forEach { (uri, shortcutId) ->
            val target = DeepLinkRouter.resolve(uri)
            assertFalse(target is DeepLinkTarget.Unknown, "shortcut '$shortcutId' deep link '$uri' is unresolved")
        }
    }
}
