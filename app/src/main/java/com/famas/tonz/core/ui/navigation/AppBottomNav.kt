package com.famas.tonz.core.ui.navigation

import android.widget.Toast
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.famas.tonz.destinations.HomeScreenDestination
import com.famas.tonz.destinations.MusicScreenDestination

@Composable
fun AppBottomNav(
    navController: NavController,
    currentRoute: String,
    allPermissionsGranted: Boolean
) {
    val context = LocalContext.current
    NavigationBar {
        BottomBarDestination.entries.forEach {
            NavigationBarItem(selected = currentRoute == it.direction.route, onClick = {
                if (!allPermissionsGranted) {
                    Toast.makeText(
                        context,
                        "Please grant the required permissions",
                        Toast.LENGTH_LONG
                    ).show()
                    return@NavigationBarItem
                }
                if (currentRoute == it.direction.route && it.direction.route != MusicScreenDestination.route) {
                    return@NavigationBarItem
                }

                if (it.direction.route == HomeScreenDestination.route) {
                    navController.popBackStack(HomeScreenDestination.route, false)
                    return@NavigationBarItem
                }

                navController.navigate(it.direction.route) {
                    popUpTo(HomeScreenDestination.route)
                }
            }, icon = {
                Icon(
                    imageVector = it.icon(currentRoute == it.direction.route),
                    contentDescription = stringResource(
                        id = it.label
                    )
                )
            }, label = {
                Text(text = stringResource(it.label))
            })
        }
    }
}