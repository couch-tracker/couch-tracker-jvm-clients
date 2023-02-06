import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.github.couchtracker.jvmclients.common.App
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.navigation.StackData


fun main() = singleWindowApplication(
    state = WindowState(width = 1080.dp, height = 800.dp)
) {
    var stackData by remember { mutableStateOf(StackData.of<Location>(Location.Home)) }
    App(stackData) { stackData = it }
}
