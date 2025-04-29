package br.com.fiap.desafioquod.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily


@Composable
fun IconCards(
    imageVectorResource: ImageVector,
    contentDescription: String,
    text: String,
    spacerHeight: Dp = 5.dp,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Text(
            text = text,
            fontFamily = recursiveFontFamily,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
        Spacer(modifier = Modifier.height(spacerHeight))
        Card(
            modifier = Modifier
                .clickable(onClick = onClick)
                .size(80.dp),
            colors = CardDefaults.cardColors(containerColor = WhiteQuod),
            border = BorderStroke(width = 1.3.dp, color = PurpleQuod),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = imageVectorResource, // Replace with your vector drawable resource
                    contentDescription = contentDescription,
                    modifier = Modifier.size(50.dp),
                    tint = GrayQuod,
                )
            }
        }
    }
}