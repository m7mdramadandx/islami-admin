import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.islami.App
import com.islami.core.di.initializeKoinModules
import com.islami.data.remote.firebase.initializeFirebase
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeFirebase()

    startKoin {
        modules(initializeKoinModules())
    }

    ComposeViewport(document.body!!) {
        App()
    }
}
