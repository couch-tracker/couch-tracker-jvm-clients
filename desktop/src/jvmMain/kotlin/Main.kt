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


fun main() {
    val driver = DriverFactory()
    application(exitProcessOnExit = true) {
        Window(
            state = WindowState(width = 1080.dp, height = 800.dp),
            onCloseRequest = ::exitApplication,
        ) {
            var stackData by remember { mutableStateOf(StackData.of<Location>(Location.Home)) }
            App(
                driver, stackData,
                close = ::exitApplication,
                editStack = { stackData = it }
            )
        }
    }
}
