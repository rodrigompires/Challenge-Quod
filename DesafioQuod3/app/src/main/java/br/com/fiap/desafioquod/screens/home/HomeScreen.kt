package br.com.fiap.desafioquod.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Scanner
import androidx.compose.material.icons.outlined.Score
import androidx.compose.material.icons.outlined.SimCardDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GreenQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily

@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(WhiteQuod)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize(),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
        ) {

            // Componente Header
            Header(iconResId = R.drawable.hbmenu, onMenuClick = {}, iconTint = BlackQuod)
            //            Fim Componente Header

            Spacer(modifier = Modifier.Companion.height(30.dp))

            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.Companion.width(380.dp)
                ) {
                    Text(
                        "_Somos uma datatech apaixonada por dados.",
                        fontFamily = recursiveFontFamily,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Companion.Thin,
                        textAlign = TextAlign.Companion.Center,
                        lineHeight = 40.sp
                    )
                }
                Spacer(modifier = Modifier.Companion.height(40.dp))
                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(320.dp)
                        .background(
                            BlackQuod
                        )
                ) {

                    Column(
                        modifier = Modifier.Companion
                            .fillMaxSize()
                            .padding(all = 10.dp)
                    ) {
                        Text(
                            "Quer saber como estamos revolucionando a segurança digital? Conheça nossas novidades em cibersegurança e antifraude.",
                            fontFamily = recursiveFontFamily,
                            color = WhiteQuod,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Companion.Normal,
                            textAlign = TextAlign.Companion.Center
                        )
                        Spacer(modifier = Modifier.Companion.height(40.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.la),
                                contentDescription = null,
                                modifier = Modifier.Companion
                                    .size(35.dp),
                                colorFilter = ColorFilter.Companion.tint(GreenQuod)
                            )
                            Spacer(modifier = Modifier.Companion.width(5.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                                        append("_")
                                    }
                                    withStyle(style = SpanStyle(color = WhiteQuod)) {
                                        append("Nossas Soluções Antifraude")
                                    }
                                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                                        append(":")
                                    }
                                },
                                fontFamily = recursiveFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Companion.Normal,
                                //textAlign = TextAlign.Center,
                                color = PurpleQuod,
                                style = TextStyle(
                                    lineHeight = 15.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.Companion.height(40.dp))

                        Row(
                            modifier = Modifier.Companion
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Box(
                                modifier = Modifier.Companion
                                    .weight(1f)
                            ) {
                                Column(

                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Companion.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Fingerprint,
                                            contentDescription = null,
                                            modifier = Modifier.Companion.size(15.dp),
                                            tint = GreenQuod,
                                        )
                                        Spacer(modifier = Modifier.Companion.width(5.dp))
                                        Text(
                                            modifier = Modifier.Companion.fillMaxWidth(),
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append("_")
                                                }
                                                withStyle(style = SpanStyle(color = WhiteQuod)) {
                                                    append("Biometria Digital")
                                                }
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append(".")
                                                }
                                            },
                                            fontFamily = recursiveFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Companion.Normal,
                                            //textAlign = TextAlign.Center,
                                            color = PurpleQuod,
                                            style = TextStyle(
                                                lineHeight = 15.sp
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.Companion.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.Companion.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccountBox,
                                            contentDescription = null,
                                            modifier = Modifier.Companion.size(15.dp),
                                            tint = GreenQuod,
                                        )
                                        Spacer(modifier = Modifier.Companion.width(5.dp))
                                        Text(
                                            modifier = Modifier.Companion.fillMaxWidth(),
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append("_")
                                                }
                                                withStyle(style = SpanStyle(color = WhiteQuod)) {
                                                    append("Biometria Facial")
                                                }
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append(".")
                                                }
                                            },
                                            fontFamily = recursiveFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Companion.Normal,
                                            //textAlign = TextAlign.Center,
                                            color = PurpleQuod,
                                            style = TextStyle(
                                                lineHeight = 15.sp
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.Companion.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.Companion.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Scanner,
                                            contentDescription = null,
                                            modifier = Modifier.Companion.size(15.dp),
                                            tint = GreenQuod,
                                        )
                                        Spacer(modifier = Modifier.Companion.width(5.dp))
                                        Text(
                                            modifier = Modifier.Companion.fillMaxWidth(),
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append("_")
                                                }
                                                withStyle(style = SpanStyle(color = WhiteQuod)) {
                                                    append("Documentoscopia")
                                                }
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append(".")
                                                }
                                            },
                                            fontFamily = recursiveFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Companion.Normal,
                                            //textAlign = TextAlign.Center,
                                            color = PurpleQuod,
                                            style = TextStyle(
                                                lineHeight = 15.sp
                                            )
                                        )
                                    }
                                }
                            }


                            Box(
                                modifier = Modifier.Companion
                                    .weight(1f)
                            ) {
                                Column(

                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Companion.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.HowToReg,
                                            contentDescription = null,
                                            modifier = Modifier.Companion.size(15.dp),
                                            tint = GreenQuod,
                                        )
                                        Spacer(modifier = Modifier.Companion.width(5.dp))
                                        Text(
                                            modifier = Modifier.Companion.fillMaxWidth(),
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append("_")
                                                }
                                                withStyle(style = SpanStyle(color = WhiteQuod)) {
                                                    append("Autenticação Cadastral")
                                                }
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append(".")
                                                }
                                            },
                                            fontFamily = recursiveFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Companion.Normal,
                                            //textAlign = TextAlign.Center,
                                            color = PurpleQuod,
                                            style = TextStyle(
                                                lineHeight = 15.sp
                                            )
                                        )
                                    }


                                    Spacer(modifier = Modifier.Companion.height(10.dp))


                                    Row(
                                        verticalAlignment = Alignment.Companion.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.SimCardDownload,
                                            contentDescription = null,
                                            modifier = Modifier.Companion.size(15.dp),
                                            tint = GreenQuod,
                                        )
                                        Spacer(modifier = Modifier.Companion.width(5.dp))
                                        Text(
                                            modifier = Modifier.Companion.fillMaxWidth(),
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append("_")
                                                }
                                                withStyle(style = SpanStyle(color = WhiteQuod)) {
                                                    append("SIM SWAP")
                                                }
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append(".")
                                                }
                                            },
                                            fontFamily = recursiveFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Companion.Normal,
                                            //textAlign = TextAlign.Center,
                                            color = PurpleQuod,
                                            style = TextStyle(
                                                lineHeight = 15.sp
                                            )
                                        )
                                    }


                                    Spacer(modifier = Modifier.Companion.height(10.dp))


                                    Row(
                                        verticalAlignment = Alignment.Companion.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Score,
                                            contentDescription = null,
                                            modifier = Modifier.Companion.size(15.dp),
                                            tint = GreenQuod,
                                        )
                                        Spacer(modifier = Modifier.Companion.width(5.dp))
                                        Text(
                                            modifier = Modifier.Companion.fillMaxWidth(),
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append("_")
                                                }
                                                withStyle(style = SpanStyle(color = WhiteQuod)) {
                                                    append("Score Antifraude")
                                                }
                                                withStyle(style = SpanStyle(color = PurpleQuod)) {
                                                    append(".")
                                                }
                                            },
                                            fontFamily = recursiveFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Companion.Normal,
                                            //textAlign = TextAlign.Center,
                                            color = PurpleQuod,
                                            style = TextStyle(
                                                lineHeight = 15.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.Companion.height(40.dp))

                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Color(0xFFF4F4F8)
                        ),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(
                        "Descubra a solução ideal para você. \nExperimente agora!",
                        textAlign = TextAlign.Companion.Center,
                        fontFamily = recursiveFontFamily,
                        color = PurpleQuod,
                        fontWeight = FontWeight.Companion.Thin,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}