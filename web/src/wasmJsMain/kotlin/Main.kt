import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.islami.App
import com.islami.core.di.initializeKoinModules
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(initializeKoinModules())
    }

    CanvasBasedWindow("Islami Admin") {
        App()
    }
}
