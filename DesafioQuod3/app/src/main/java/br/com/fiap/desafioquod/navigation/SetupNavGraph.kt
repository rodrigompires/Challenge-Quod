package br.com.fiap.desafioquod.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.fiap.desafioquod.screens.digitalbiometrics.DigitalBiometricsScreen
import br.com.fiap.desafioquod.screens.digitalbiometrics.DigitalBiometricsValidationScreen
import br.com.fiap.desafioquod.screens.documentcospy.DocumentcospyScreen
import br.com.fiap.desafioquod.screens.documentcospy.DocumentcospyValidationScreen
import br.com.fiap.desafioquod.screens.documentcospy.ImagePreviewDocScreen
import br.com.fiap.desafioquod.screens.facialbiometrics.FacialBiometricsScreen
import br.com.fiap.desafioquod.screens.facialbiometrics.FacialBiometricsValidationScreen
import br.com.fiap.desafioquod.screens.facialbiometrics.FacialCapture
import br.com.fiap.desafioquod.screens.facialbiometrics.ImagePreviewScreen
import br.com.fiap.desafioquod.screens.home.HomeScreen
import br.com.fiap.desafioquod.screens.main.MainScreen
import br.com.fiap.desafioquod.screens.registrationauthentication.RegistrationAuthenticationScreen
import br.com.fiap.desafioquod.screens.registrationauthentication.RegistrationAuthenticationValidationScreen
import br.com.fiap.desafioquod.screens.score.ScoreScreen
import br.com.fiap.desafioquod.screens.simswap.SimSwapScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main",
    ) {
        composable(
            route = "main",
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1000)) + fadeOut(tween(2000))
            },
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(1000)) + fadeIn(tween(2000))
            }
        ) {
            MainScreen(navController)
        }

        composable(
            route = "home",
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1000)) + fadeOut(tween(2000))
            },
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(1000)) + fadeIn(tween(2000))
            }
        ) {
            HomeScreen(navController)
        }

//        Tela Biometria Dgitial

        composable(
            route = "digital",
        ) {
            DigitalBiometricsScreen(navController)
        }
//        Fim Tela Biometria Digital

//        Tela Validação Digital
        composable(
            route = "digitalvalidation/{status}?analysisReport={analysisReport}&message={message}",
            arguments = listOf(
                navArgument("status") {
                    type = NavType.StringType
                    nullable = false  // Ensure this matches your function signature
                },
                navArgument("analysisReport") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("message") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status")
            val analysisReport = backStackEntry.arguments?.getString("analysisReport")
            val message = backStackEntry.arguments?.getString("message")
            DigitalBiometricsValidationScreen(
                navController = navController,
                status = status,
                analysisReport = analysisReport,
                message = message
            )
        }
//      Fim Tela Validação Digital



        composable(
            route = "facial",
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1000)) + fadeOut(tween(2000))
            },
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(1000)) + fadeIn(tween(2000))
            }
        ) {
            FacialBiometricsScreen(navController)
        }

        //  Rota da tela de captura da imagem do rosto
        composable(
            route = "facialCapture",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(1000)
                ) + fadeIn(tween(2000))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 1200, delayMillis = 300)) +
                        scaleOut(
                            targetScale = 1.5f, // Corresponde ao captureScale final e à enterTransition
                            animationSpec = tween(durationMillis = 1200)
                        )
            }
        ) {
            FacialCapture(navController)
        }

        composable(
            "imagePreview/{firstUri}/{secondUri}/{firstLat}/{firstLon}/{secondLat}/{secondLon}",
            arguments = listOf(
                navArgument("firstUri") { type = NavType.StringType },
                navArgument("secondUri") { type = NavType.StringType },
                navArgument("firstLat") { type = NavType.StringType },
                navArgument("firstLon") { type = NavType.StringType },
                navArgument("secondLat") { type = NavType.StringType },
                navArgument("secondLon") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 1200)) +
                        scaleIn(
                            initialScale = 1.5f, // Corresponde ao captureScale final
                            animationSpec = tween(durationMillis = 1200)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 1500)) +
                        scaleOut(animationSpec = tween(durationMillis = 1500))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 1500)) +
                        scaleIn(animationSpec = tween(durationMillis = 1500))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 1500)) +
                        scaleOut(animationSpec = tween(durationMillis = 1500))
            }
        ) { backStackEntry ->
            val firstUri = backStackEntry.arguments?.getString("firstUri")
            val secondUri = backStackEntry.arguments?.getString("secondUri")
            val firstLat = backStackEntry.arguments?.getString("firstLat")
            val firstLon = backStackEntry.arguments?.getString("firstLon")
            val secondLat = backStackEntry.arguments?.getString("secondLat")
            val secondLon = backStackEntry.arguments?.getString("secondLon")
            ImagePreviewScreen(
                navController = navController,
                firstUri = firstUri,
                secondUri = secondUri,
                firstLat = firstLat,
                firstLon = firstLon,
                secondLat = secondLat,
                secondLon = secondLon
            )
        }


        composable(
            route = "facialvalidation?imageName={imageName}",
            enterTransition = {
                fadeIn(animationSpec = tween(2000))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(2000))
            }
        ) { backStackEntry ->
            val imageName = backStackEntry.arguments?.getString("imageName")
            FacialBiometricsValidationScreen(navController, imageName)
        }

        composable(
            route = "documentcospy",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(1000)) + fadeIn(tween(2000))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1000)) + fadeOut(tween(2000))
            }
        ) {
            DocumentcospyScreen(navController)
        }

        composable(
            "imagepreviewdocscreen/{selectedDocumentType}/{frontImageUri}/{backImageUri}/{latitudeFront}/{longitudeFront}/{latitudeBack}/{longitudeBack}",
            arguments = listOf(
                navArgument("selectedDocumentType") { type = NavType.StringType },
                navArgument("frontImageUri") { type = NavType.StringType },
                navArgument("backImageUri") { type = NavType.StringType },
                navArgument("latitudeFront") { type = NavType.StringType },
                navArgument("longitudeFront") { type = NavType.StringType },
                navArgument("latitudeBack") { type = NavType.StringType },
                navArgument("longitudeBack") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val selectedDocumentType = backStackEntry.arguments?.getString("selectedDocumentType")
            val frontImageUri = backStackEntry.arguments?.getString("frontImageUri")
            val backImageUri = backStackEntry.arguments?.getString("backImageUri")
            val latitudeFront = backStackEntry.arguments?.getString("latitudeFront")
            val longitudeFront = backStackEntry.arguments?.getString("longitudeFront")
            val latitudeBack = backStackEntry.arguments?.getString("latitudeBack")
            val longitudeBack = backStackEntry.arguments?.getString("longitudeBack")

            ImagePreviewDocScreen(
                navController = navController,
                selectedDocumentType = selectedDocumentType,
                frontImageUri = frontImageUri,
                backImageUri = backImageUri,
                latitudeFront = latitudeFront,
                longitudeFront = longitudeFront,
                latitudeBack = latitudeBack,
                longitudeBack = longitudeBack
            )
        }
        composable(
            route = "documentValidation",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(1000)) + fadeIn(tween(2000))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1000)) + fadeOut(tween(2000))
            }
        ) {
            DocumentcospyValidationScreen(navController)
        }

        composable(
            route = "score",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(1000)) + fadeIn(tween(2000))
            }
        ) {
            ScoreScreen(navController)
        }

        composable(
            route = "registration",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(1000)) + fadeIn(tween(2000))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1000)) + fadeOut(tween(2000))
            }
        ) {
            RegistrationAuthenticationScreen(navController)
        }

        composable(
            route = "registrationvalidation?selectedOption={selectedOption}",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(1000)) + fadeIn(tween(2000))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1000)) + fadeOut(tween(2000))
            }
        ) { backStackEntry ->
            val selectedOption = backStackEntry.arguments?.getString("selectedOption")
            RegistrationAuthenticationValidationScreen(navController, selectedOption)
        }

        composable(
            route = "simswap",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(1000)) + fadeIn(tween(2000))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1000)) + fadeOut(tween(2000))
            }
        ) {
            SimSwapScreen(navController)
        }
    }
}
