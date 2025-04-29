package br.com.fiap.desafioquod


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import br.com.fiap.desafioquod.navigation.BottomNav
import br.com.fiap.desafioquod.navigation.SetupNavGraph
import br.com.fiap.desafioquod.ui.theme.DesafioQuodTheme
//import androidx.appcompat.app.AppCompatActivity

class MainActivity : FragmentActivity () {

    private var isBottomBarVisible by mutableStateOf(true)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DesafioQuodTheme {
                val navController = rememberNavController()

                LaunchedEffect(navController) {
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        isBottomBarVisible = destination.route != "main"
                    }
                }
                Scaffold(
                    bottomBar = {
                        if (isBottomBarVisible) {
                            BottomNav(navController = navController)
                        }
                    }
                ) {
                    SetupNavGraph(navController = navController)
                }
            }
        }
    }
}





//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun HomeScreenPreview() {
//    DesafioQuodTheme {
//        val navController = rememberNavController()
//        MenuScreen(navController)
//    }
//}
