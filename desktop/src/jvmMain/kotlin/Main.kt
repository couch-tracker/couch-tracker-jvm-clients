import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.github.couchtracker.jvmclients.common.App
import com.github.couchtracker.jvmclients.common.DriverFactory
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.ui.screen.HomeLocation


fun main() {
    val driver = DriverFactory()
    application(exitProcessOnExit = true) {
        var stackData by remember { mutableStateOf(StackData.of<Location>(HomeLocation)) }
        val title = buildString {
            append("Couch tracker")
            val top = stackData.stack.last().title
            if (top != null) {
                append(" - ")
                append(top)
            }
        }
        Window(
            state = remember { WindowState(width = 1080.dp, height = 800.dp) },
            title = title,
            onCloseRequest = ::exitApplication,
        ) {
            App(
                driver, stackData,
                editStack = {
                    if (it == null) exitApplication()
                    else stackData = it
                },
            )
        }
    }
}
