import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.singleWindowApplication
import com.github.couchtracker.jvmclients.common.App
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.navigation.StackData


fun main() = singleWindowApplication {
    var stackData by remember { mutableStateOf(StackData.of(Location.Home)) }
    App(stackData) { stackData = stackData.it() }
}
