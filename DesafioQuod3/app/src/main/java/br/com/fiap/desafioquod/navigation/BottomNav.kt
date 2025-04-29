package br.com.fiap.desafioquod.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import br.com.fiap.desafioquod.components.Screens
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily

@Composable
fun BottomNav(
    navController: NavHostController
) {
    var selectedIndex by remember {
        mutableIntStateOf(3)
    }

    val list = listOf(
        Screens.Facial,
        Screens.Documentcospy,
        Screens.Digital,
        Screens.Home,
        Screens.Score,
        Screens.Registration,
        Screens.Sim,

    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            //.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(width = 1.dp, Color(0xFFF1ECEC))
        //.border(width = 1.dp, BlackQuod)
    ) {
        NavigationBar(
            containerColor = Color(0xFFfffffc),
        ) {
            list.forEachIndexed { index, screens ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = {
                        navController.navigate(screens.route)
                        selectedIndex = index
                    },
                    icon = {
                        Icon(
                            imageVector = screens.imageVector,
                            contentDescription = "",
                            tint = if (selectedIndex == index) PurpleQuod else BlackQuod,
                            modifier = Modifier
                                .scale(if (selectedIndex == index) 1.5f else 1f)
                        )
                    },
                    label = {
                        Text(
                            screens.label,
                            fontFamily = recursiveFontFamily,
                            textAlign = TextAlign.Center,
                            color = PurpleQuod,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }
        }
    }
}