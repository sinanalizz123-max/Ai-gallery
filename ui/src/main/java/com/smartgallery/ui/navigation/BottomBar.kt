package com.smartgallery.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.smartgallery.ui.R

private data class BottomItem(
    val route: Routes,
    val label: String,
    val icon: ImageVector
)

@Composable
fun SmartBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(
        BottomItem(Routes.Photos, "Photos", ImageVector.vectorResource(R.drawable.ic_photos)),
        BottomItem(Routes.People, "People", ImageVector.vectorResource(R.drawable.ic_people)),
        BottomItem(Routes.Albums, "Albums", ImageVector.vectorResource(R.drawable.ic_albums)),
        BottomItem(Routes.Ai, "AI", ImageVector.vectorResource(R.drawable.ic_ai))
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route.route,
                onClick = { onNavigate(item.route.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
