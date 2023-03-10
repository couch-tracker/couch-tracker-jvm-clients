import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.github.couchtracker.jvmclients.common.App
import com.github.couchtracker.jvmclients.common.DriverFactory
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.ui.screen.HomeLocation

fun main() {
    val driver = DriverFactory()
    application(exitProcessOnExit = true) {
        var stackData by remember { mutableStateOf(StackData.of<Location>(HomeLocation)) }
        Window(
            state = remember { WindowState(width = 1080.dp, height = 800.dp) },
            title = "Couch tracker",
            onCloseRequest = ::exitApplication,
        ) {
            App(
                driver,
                stackData,
                editStack = {
                    when (it) {
                        null -> exitApplication()
                        else -> stackData = it
                    }
                },
            )
        }
    }
}
