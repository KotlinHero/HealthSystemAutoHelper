package tech.kotlinhero.autohelper.ui.route

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

interface RouterItem {
    val label: String
    val icon: ImageVector
    val route: String
}

enum class NavigationRouter(
    override val label: String,
    override val icon: ImageVector,
    override val route: String
) : RouterItem {
    TaskMode("任务模式", Icons.Filled.GridView, "mode"),
    TaskHistory("任务历史", Icons.Filled.History, "history"),
    Settings("设置", Icons.Filled.Settings, "settings")
}

object TaskExecuteRouterItem : RouterItem {
    override val route: String = "execute"
    override val label: String = "任务执行"
    override val icon: ImageVector = Icons.Filled.GridView
}