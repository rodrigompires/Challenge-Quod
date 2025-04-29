package br.com.fiap.desafioquod.screens.simswap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.CustomButton
import br.com.fiap.desafioquod.components.DraggableButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily

@Composable
fun SimSwapScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var codigoValidacao by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var dateBirth by remember { mutableStateOf(TextFieldValue("")) }
    var message by remember { mutableStateOf("") }
    var showDialogMessage by remember { mutableStateOf(false) }
    var imageResource by remember { mutableStateOf(0) }


    fun formatPhoneNumber(phoneNumber: String): String {
        // Remove todos os caracteres não numéricos
        val cleaned = phoneNumber.replace("\\D".toRegex(), "")

        return when {
            cleaned.length >= 11 -> "(${cleaned.substring(0, 2)}) ${
                cleaned.substring(
                    2,
                    7
                )
            }-${cleaned.substring(7, 11)}"

            cleaned.length >= 7 -> "(${cleaned.substring(0, 2)}) ${
                cleaned.substring(
                    2,
                    7
                )
            }-${cleaned.substring(7)}"

            cleaned.length >= 3 -> "(${cleaned.substring(0, 2)}) ${cleaned.substring(2)}"
            cleaned.length >= 2 -> "(${cleaned.substring(0, 2)}) "
            else -> cleaned
        }
    }


    fun formatDateBirth(dateBirthNumber: String): String {
        val cleaned = dateBirthNumber.replace("\\D".toRegex(), "")
        return buildString {
            for (i in cleaned.indices) {
                append(cleaned[i])
                if (i == 1 || i == 3) {
                    append("/")
                }
            }
        }
    }


    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(WhiteQuod)
    ) {

        DraggableButton(
            bgColor = PurpleQuod,
            onClick = {
                showDialog = true
            },
        )

        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(WhiteQuod),
            //.background(Color(0Xffededed)),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
//            Componente Header
            Header(
                iconResId = R.drawable.hbmenu,
                onMenuClick = {
                    navController.navigate("home")
                },
                iconTint = GrayQuod
            )
//            Fim Componente Header

            Spacer(modifier = Modifier.Companion.height(50.dp))

            Text(
                modifier = Modifier.Companion.fillMaxWidth(),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append("_")
                    }
                    withStyle(style = SpanStyle(color = BlackQuod)) {
                        append("SIM SWAP")
                    }
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append(".")
                    }
                },
                fontFamily = recursiveFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.Companion.Normal,
                textAlign = TextAlign.Companion.Center,
                color = GrayQuod,
                style = TextStyle(
                    lineHeight = 38.sp
                )
            )

            Spacer(modifier = Modifier.Companion.height(20.dp))

            Image(
                painter = painterResource(R.drawable.smartphone),
                contentDescription = null,
                modifier = Modifier.Companion.size(110.dp)
            )

            Spacer(modifier = Modifier.Companion.height(10.dp))

            Text(
                "Verifique se houve troca recente de chip para prevenir fraudes!",
                fontFamily = recursiveFontFamily,
                fontSize = 18.sp,
                color = BlackQuod,
                textAlign = TextAlign.Companion.Center,
                fontWeight = FontWeight.Companion.Bold
            )

            Card(
                modifier = Modifier.Companion
                    //.width(200.dp)
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0Xffededed)),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(width = 0.1.dp, color = PurpleQuod)
            ) {
                Box( // Ensures content fills the card
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Column(
                        modifier = Modifier.Companion.fillMaxSize(),
                        horizontalAlignment = Alignment.Companion.CenterHorizontally,
                        //verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { newValue ->
                                // Remove caracteres não numéricos
                                val numericValue = newValue.text.filter { it.isDigit() }

                                // Permite apenas a atualização se tiver pelo menos 11 dígitos
                                if (numericValue.length <= 11) {
                                    // Aplica a máscara utilizando a função formatPhoneNumber
                                    val maskedValue = formatPhoneNumber(numericValue)

                                    // Atualiza o valor do telefone
                                    // Ajusta o cursor para o final da string formatada
                                    phone =
                                        TextFieldValue(maskedValue, TextRange(maskedValue.length))
                                }

                                // Fecha o teclado quando chegar a 11 dígitos
                                if (numericValue.length == 11) {
                                    keyboardController?.hide()
                                }
                            },
                            label = {
                                Text(
                                    "Numero de Telefone Atual",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            placeholder = {
                                Text(
                                    "Digite o seu telefone",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BlackQuod,
                                focusedBorderColor = PurpleQuod,
                                focusedTextColor = BlackQuod,
                                unfocusedTextColor = BlackQuod,
                                unfocusedContainerColor = WhiteQuod,
                                focusedContainerColor = WhiteQuod,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
                            textStyle = TextStyle(
                                fontFamily = recursiveFontFamily,
                                fontSize = 16.sp,
                                color = BlackQuod,
                                fontWeight = FontWeight.Companion.Bold
                            ),
                            singleLine = true,
                            modifier = Modifier.Companion.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.Companion.height(8.dp))



                        OutlinedTextField(
                            value = dateBirth,
                            onValueChange = { newValue ->
                                // Remove caracteres não numéricos
                                val numericValue = newValue.text.filter { it.isDigit() }

                                // Permite apenas a atualização se tiver pelo menos 11 dígitos
                                if (numericValue.length <= 8) {
                                    // Aplica a máscara utilizando a função formatPhoneNumber
                                    val maskedValue = formatDateBirth(numericValue)

                                    // Atualiza o valor do telefone
                                    // Ajusta o cursor para o final da string formatada
                                    dateBirth =
                                        TextFieldValue(maskedValue, TextRange(maskedValue.length))
                                }

                                // Fecha o teclado quando chegar a 11 dígitos
                                if (numericValue.length == 8) {
                                    keyboardController?.hide()
                                }
                            },
                            label = {
                                Text(
                                    "Data de Nascimento",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            placeholder = {
                                Text(
                                    "dd/mm/aaa",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BlackQuod,
                                focusedBorderColor = PurpleQuod,
                                focusedTextColor = BlackQuod,
                                unfocusedTextColor = BlackQuod,
                                unfocusedContainerColor = WhiteQuod,
                                focusedContainerColor = WhiteQuod,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
                            textStyle = TextStyle(
                                fontFamily = recursiveFontFamily,
                                fontSize = 16.sp,
                                color = BlackQuod,
                                fontWeight = FontWeight.Companion.Bold
                            ),
                            singleLine = true,
                            modifier = Modifier.Companion.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.Companion.height(8.dp))

                        OutlinedTextField(
                            value = codigoValidacao,
                            onValueChange = { codigoValidacao = it },
                            label = {
                                Text(
                                    "Código de Validação",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            placeholder = {
                                Text(
                                    "Para a validação OK, digite o codigo 1234",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BlackQuod,
                                focusedBorderColor = PurpleQuod,
                                focusedTextColor = BlackQuod,
                                unfocusedTextColor = BlackQuod,
                                unfocusedContainerColor = WhiteQuod,
                                focusedContainerColor = WhiteQuod,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
                            textStyle = TextStyle(
                                fontFamily = recursiveFontFamily,
                                fontSize = 16.sp,
                                color = BlackQuod,
                                fontWeight = FontWeight.Companion.Bold
                            ),
                            singleLine = true,
                            modifier = Modifier.Companion.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.Companion.height(16.dp))

                        CustomButton(
                            modifier = Modifier.Companion
                                .padding(top = 16.dp)
                                .width(130.dp),
                            color = PurpleQuod,
                            borderWith = 0.5.dp,
                            borderColor = PurpleQuod,
                            onClick = {
                                // Validação simples dos dados
                                if (phone.text.isNotEmpty() && dateBirth.text.isNotEmpty() && codigoValidacao == "1234") {
                                    message =
                                        "Validação bem-sucedida! \nA troca de SIM foi confirmada."
                                    imageResource = R.drawable.ok
                                } else {
                                    message = "Erro: Verifique suas informações e tente novamente."
                                    imageResource = R.drawable.error
                                }
                                showDialogMessage = true
                            },
                            cornerRadius = 10.dp,
                            textStyle = TextStyle(
                                color = WhiteQuod,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Companion.Medium
                            ),
                            buttonText = "Analisar",
                            enabled = phone.text.isNotEmpty() && dateBirth.text.isNotEmpty() && codigoValidacao.isNotEmpty()
                        )


                        if (showDialogMessage) {
                            Dialog(onDismissRequest = { showDialogMessage = false }) {
                                Card(
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Companion.White)
                                ) {
                                    Column(
                                        modifier = Modifier.Companion.padding(16.dp),
                                        horizontalAlignment = Alignment.Companion.CenterHorizontally
                                    ) {

                                        Image(
                                            painter = painterResource(id = imageResource),
                                            contentDescription = "Image",
                                            modifier = Modifier.Companion.size(60.dp)
                                        )

                                        Spacer(modifier = Modifier.Companion.height(30.dp))
                                        Text(
                                            text = message,
                                            fontWeight = FontWeight.Companion.Bold,
                                            fontFamily = recursiveFontFamily,
                                            textAlign = TextAlign.Companion.Center,
                                            color = Color.Companion.Black
                                        )

                                        Spacer(modifier = Modifier.Companion.height(30.dp))

                                        Button(
                                            onClick = { showDialogMessage = false },
                                            colors = ButtonDefaults.buttonColors(BlackQuod)
                                        ) {
                                            Text("Fechar", fontFamily = recursiveFontFamily)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Companion.White)
            ) {
                Column(
                    modifier = Modifier.Companion.padding(16.dp),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Text(
                        text = "O sim swap é um golpe em que um criminoso se passa por você para transferir seu número de celular para outro chip. " +
                                "Com isso, ele pode ter acesso a seus aplicativos de banco, redes sociais e outras informações importantes, " +
                                "podendo causar prejuízos financeiros e pessoais.",
                        fontWeight = FontWeight.Companion.Bold,
                        fontFamily = recursiveFontFamily,
                        textAlign = TextAlign.Companion.Center,
                        color = Color.Companion.Black
                    )
                    Spacer(modifier = Modifier.Companion.height(8.dp))
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(BlackQuod)
                    ) {
                        Text("Fechar", fontFamily = recursiveFontFamily)
                    }
                }
            }
        }
    }
}