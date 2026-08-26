package com.kelvinsaputra.tvpulse

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelvinsaputra.tvpulse.ui.navigation.DeepLinkEvent
import com.kelvinsaputra.tvpulse.ui.navigation.DeepLinkTarget
import com.kelvinsaputra.tvpulse.ui.navigation.TvPulseApp
import com.kelvinsaputra.tvpulse.ui.theme.TVPulseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val deepLinkEvent = MutableStateFlow<DeepLinkEvent?>(null)
    private var deepLinkSequence = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)

        setContent {
            TVPulseTheme {
                val event by deepLinkEvent.collectAsStateWithLifecycle()
                TvPulseApp(
                    deepLinkEvent = event,
                    onExit = ::finish,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val target = intent?.toDeepLinkTargetOrNull() ?: return
        deepLinkSequence += 1
        deepLinkEvent.value = DeepLinkEvent(
            target = target,
            sequence = deepLinkSequence,
        )
    }
}

private fun Intent.toDeepLinkTargetOrNull(): DeepLinkTarget? {
    if (action != Intent.ACTION_VIEW) return null

    val uri = data ?: return null
    if (uri.scheme != DEEP_LINK_SCHEME) return null

    return when (uri.host?.lowercase()) {
        "home" -> DeepLinkTarget.Home

        "favorites",
        "favorite",
        -> DeepLinkTarget.Favorites

        "search" -> DeepLinkTarget.Search(
            query = uri.getQueryParameter("query")
                ?: uri.getQueryParameter("q")
                ?: uri.pathSegments.firstOrNull(),
        )

        "detail" -> uri.pathSegments
            .singleOrNull()
            ?.toLongOrNull()
            ?.takeIf { it > 0L }
            ?.let { showId -> DeepLinkTarget.Detail(showId) }

        else -> null
    }
}

private const val DEEP_LINK_SCHEME = "movieapp"
