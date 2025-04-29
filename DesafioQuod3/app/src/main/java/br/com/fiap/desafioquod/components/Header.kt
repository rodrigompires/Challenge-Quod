package br.com.fiap.desafioquod.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.ui.theme.WhiteQuod

@Composable
fun Header(
    iconResId: Int,
    onMenuClick: () -> Unit,
    iconTint: Color
) {
    Column (
        Modifier
            .fillMaxWidth().background(WhiteQuod)
    ) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Quod",
            modifier = Modifier.size(120.dp)
        )

        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "Menu",
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onMenuClick),
            colorFilter = ColorFilter.tint(iconTint)
        )

    }
    Divider(
        color = Color(0xFFE6E6E6),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 16.dp) // Adjust padding as needed
    )
    }
}