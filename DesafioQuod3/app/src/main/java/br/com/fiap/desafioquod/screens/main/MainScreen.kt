package br.com.fiap.desafioquod.screens.main

import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay

@Composable
fun MainScreen(navController: NavController) {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(WhiteQuod)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(Color(0xFF000000)),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.marca),
                contentDescription = "Marca Quod",
                modifier = Modifier.Companion.size(200.dp)
            )

            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        val widthInDp = 200.dp
                        val heightInDp = 200.dp
                        layoutParams = ViewGroup.LayoutParams(
                            TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                widthInDp.value,
                                context.resources.displayMetrics
                            ).toInt(),
                            TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                heightInDp.value,
                                context.resources.displayMetrics
                            ).toInt()
                        )
                    }
                },
                update = { imageView ->
                    Glide.with(imageView.context)
                        .asGif()
                        .load(R.drawable.initialloading)
                        .into(imageView)
                }
            )
            LaunchedEffect(Unit) {
                delay(4000)
                navController.navigate("home")
            }
        }
    }
}