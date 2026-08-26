package com.kelvinsaputra.tvpulse

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelvinsaputra.tvpulse.ui.navigation.DeepLinkEvent
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
        val showId = intent?.toDetailShowIdOrNull() ?: return
        deepLinkSequence += 1
        deepLinkEvent.value = DeepLinkEvent(
            showId = showId,
            sequence = deepLinkSequence,
        )
    }
}

private fun Intent.toDetailShowIdOrNull(): Long? {
    if (action != Intent.ACTION_VIEW) return null

    val uri = data ?: return null
    if (uri.scheme != "tvpulse-kelvin" || uri.host != "detail") return null

    return uri.pathSegments
        .singleOrNull()
        ?.toLongOrNull()
        ?.takeIf { it > 0L }
}
