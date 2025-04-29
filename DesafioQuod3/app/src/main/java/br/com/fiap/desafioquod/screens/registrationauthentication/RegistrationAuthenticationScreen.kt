package br.com.fiap.desafioquod.screens.registrationauthentication

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.CustomButton
import br.com.fiap.desafioquod.components.DraggableButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun RegistrationAuthenticationScreen(navController: NavController) {

    //var isScanning by remember { mutableStateOf(false) }
    //var isScanningError by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var cpfText by remember { mutableStateOf(TextFieldValue("")) }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    var selectedOption by remember { mutableStateOf("OK") } // Estado para armazenar a opção selecionada

    // Função de formatação do CPF
    fun formatCPF(cpf: String): String {
        val numbers = cpf.replace("\\D".toRegex(), "")
        return when {
            numbers.length <= 3 -> numbers
            numbers.length <= 6 -> "${numbers.substring(0, 3)}.${numbers.substring(3)}"
            numbers.length <= 9 -> "${numbers.substring(0, 3)}.${
                numbers.substring(
                    3,
                    6
                )
            }.${numbers.substring(6)}"

            numbers.length <= 11 -> "${numbers.substring(0, 3)}.${
                numbers.substring(
                    3,
                    6
                )
            }.${numbers.substring(6, 9)}-${numbers.substring(9)}"

            else -> numbers.substring(0, 11)
        }
    }


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

    val isButtonEnabled = phone.text.isNotEmpty() && cpfText.text.isNotEmpty() && name.isNotEmpty() && address.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteQuod)
    ) {

        DraggableButton(
            bgColor = PurpleQuod,
            onClick = {
                showDialog = true
            },
            //modifier = Modifier.align(Alignment.CenterEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                //.padding(top = 30.dp, start = 16.dp, bottom = 20.dp, end = 16.dp)
                .background(WhiteQuod),
//                .background(Color(0Xffededed)),
            horizontalAlignment = Alignment.CenterHorizontally
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

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append("_")
                    }
                    withStyle(style = SpanStyle(color = BlackQuod)) {
                        append("Autenticação Cadastral")
                    }
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append(".")
                    }
                },
                fontFamily = recursiveFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = GrayQuod,
                style = TextStyle(
                    lineHeight = 50.sp
                )
            )

            Spacer(modifier = Modifier.height(50.dp))

            Card(
                modifier = Modifier
                    //.width(200.dp)
                    .fillMaxWidth()
                    .height(450.dp)
                    .padding(16.dp),
                    //.background(Color(0Xffededed)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0Xffededed)),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(width = 0.1.dp, color = PurpleQuod)
            ) {
                Box( // Ensures content fills the card
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = cpfText,
                            onValueChange = { newText ->
                                val formattedCpf = formatCPF(newText.text)
                                cpfText =
                                    TextFieldValue(
                                        formattedCpf,
                                        selection = TextRange(formattedCpf.length)
                                    )
                                if (formattedCpf.length == 14) { // Considerando que o CPF formatado tem 14 caracteres ("xxx.xxx.xxx-xx")
                                    keyboardController?.hide() // Fecha o teclado
                                }
                            },
                            label = {
                                Text(
                                    "CPF",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            placeholder = {
                                Text(
                                    "Didite o seu CPF",
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                fontFamily = recursiveFontFamily,
                                fontSize = 16.sp,
                                color = BlackQuod,
                                fontWeight = FontWeight.Bold
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                            },
                            label = {
                                Text(
                                    "Nome",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            placeholder = {
                                Text(
                                    "Didite o seu nome",
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            textStyle = TextStyle(
                                fontFamily = recursiveFontFamily,
                                fontSize = 16.sp,
                                color = BlackQuod,
                                fontWeight = FontWeight.Bold
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { newValue ->
                                // Remove non-numeric characters
                                val numericValue = newValue.text.filter { it.isDigit() }

                                // Limita a 11 dígitos
                                if (numericValue.length <= 11) {
                                    // Aplica a máscara utilizando a função formatPhoneNumber
                                    val maskedValue = formatPhoneNumber(numericValue)

                                    // Atualiza o valor do telefone
                                    // Ajusta o cursor para o final da string formatada
                                    phone =
                                        TextFieldValue(maskedValue, TextRange(maskedValue.length))
                                }
                                if (numericValue.length == 11) { // Considerando que o CPF formatado tem 14 caracteres ("xxx.xxx.xxx-xx")
                                    keyboardController?.hide() // Fecha o teclado
                                }
                            },
                            label = {
                                Text(
                                    "Telefone",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            placeholder = {
                                Text(
                                    "Didite o seu telefone",
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                fontFamily = recursiveFontFamily,
                                fontSize = 16.sp,
                                color = BlackQuod,
                                fontWeight = FontWeight.Bold
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = {
                                address = it
                            },
                            label = {
                                Text(
                                    "Endereço",
                                    fontFamily = recursiveFontFamily,
                                    color = PurpleQuod
                                )
                            },
                            placeholder = {
                                Text(
                                    "Didite o seu endereço",
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            textStyle = TextStyle(
                                fontFamily = recursiveFontFamily,
                                fontSize = 16.sp,
                                color = BlackQuod,
                                fontWeight = FontWeight.Bold
                            ),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOption == "OK",
                                    onClick = { selectedOption = "OK" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PurpleQuod,
                                        unselectedColor = BlackQuod
                                    ),
                                )
                                Text("OK", fontFamily = recursiveFontFamily)
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOption == "Não OK",
                                    onClick = { selectedOption = "Não OK" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PurpleQuod,
                                        unselectedColor = BlackQuod
                                    )
                                )
                                Text("Não OK", fontFamily = recursiveFontFamily)
                            }

                        }



                        CustomButton(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .width(130.dp),
                            color = PurpleQuod,
                            borderWith = 0.5.dp,
                            borderColor = PurpleQuod,
                            onClick = {
                                if (selectedOption == "OK") {
                                    coroutineScope.launch {
                                        delay(500)
                                        navController.navigate("registrationvalidation?selectedOption=${selectedOption}")
                                    }
                                } else {
                                    coroutineScope.launch {
                                        delay(500)
                                        navController.navigate("registrationvalidation?selectedOption=${selectedOption}")
                                    }
                                }


                            },
                            cornerRadius = 10.dp,
                            textStyle = TextStyle(
                                color = WhiteQuod,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            buttonText = "Analisar",
                            enabled = isButtonEnabled
                        )


                    }
                }
            }




            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Card(
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Autenticação cadastral é o processo de verificar se as informações fornecidas por uma " +
                                        "pessoa durante um cadastro, como nome e CPF, são verdadeiras e confiáveis, " +
                                        "garantindo que a pessoa seja quem ela diz ser.",
                                fontWeight = FontWeight.Bold,
                                fontFamily = recursiveFontFamily,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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


    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistrationAuthenticationScreenPreview() {
    val navController = rememberNavController()
    RegistrationAuthenticationScreen(navController)
}