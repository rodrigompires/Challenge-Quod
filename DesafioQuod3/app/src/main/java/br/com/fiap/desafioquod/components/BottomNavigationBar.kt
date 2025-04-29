package br.com.fiap.desafioquod.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Scanner
import androidx.compose.material.icons.outlined.Score
import androidx.compose.material.icons.outlined.SimCardDownload
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.composable
import br.com.fiap.desafioquod.screens.digitalbiometrics.DigitalBiometricsScreen
import br.com.fiap.desafioquod.screens.documentcospy.DocumentcospyScreen
import br.com.fiap.desafioquod.screens.facialbiometrics.FacialBiometricsScreen
import br.com.fiap.desafioquod.screens.home.HomeScreen
import br.com.fiap.desafioquod.screens.registrationauthentication.RegistrationAuthenticationScreen
import br.com.fiap.desafioquod.screens.score.ScoreScreen
import br.com.fiap.desafioquod.screens.simswap.SimSwapScreen

@Composable
fun BottomNavigationBar(
    navHostController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        builder = {
            composable(NavCons.facial) {
                FacialBiometricsScreen(navController = navHostController)
            }
            composable(NavCons.score) {
                ScoreScreen(navController = navHostController)
            }
            composable(NavCons.digital) {
                DigitalBiometricsScreen(navController = navHostController)
            }
            composable(NavCons.documentcospy) {
                DocumentcospyScreen(navController = navHostController)
            }
            composable(NavCons.registration) {
                RegistrationAuthenticationScreen(navController = navHostController)
            }
            composable(NavCons.sim) {
                SimSwapScreen(navController = navHostController)
            }
            composable(NavCons.home) {
                HomeScreen(navController = navHostController)
            }
        }
    )
}

sealed class Screens(val route: String, val imageVector: ImageVector, val label: String) {

    object Digital : Screens(
        route = NavCons.digital,
        label = "Biometria Digital",
        imageVector = Icons.Outlined.Fingerprint
    )

    object Sim : Screens(
        route = NavCons.sim,
        label = "SIM SWAP",
        imageVector = Icons.Outlined.SimCardDownload
    )

    object Facial : Screens(
        route = NavCons.facial,
        label = "Biometria facial",
        imageVector = Icons.Outlined.AccountBox
    )

    object Home : Screens(
        route = NavCons.home,
        label = "Home",
        imageVector = Icons.Outlined.Menu
    )

    object Documentcospy : Screens(
        route = NavCons.documentcospy,
        label = "Documentoscopia",
        imageVector = Icons.Outlined.Scanner
    )

    object Score : Screens(
        route = NavCons.score,
        label = "Score",
        imageVector = Icons.Outlined.Score
    )

    object Registration : Screens(
        route = NavCons.registration,
        label = "Análise Cadastral",
        imageVector = Icons.Outlined.HowToReg
    )


}

object NavCons {

    const val digital = "digital"
    const val sim = "simswap"
    const val facial = "facial"
    const val home = "home"
    const val documentcospy = "documentcospy"
    const val score = "score"
    const val registration = "registration"
}