package br.com.fiap.desafioquod.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily



@Composable
fun CustomDropdown(selectedUsername: String, onUsernameSelected: (String) -> Unit) {

    val isDropDownExpanded = remember {
        mutableStateOf(false)
    }

    val itemPosition = remember {
        mutableStateOf(0)
    }

    val usernames = listOf(
        "Escolha uma digital",
        "Digital 1 - OK",
        "Digital 2 - ERRO",
        "Digital 3 - OK",
        "Digital 4 - ERRO",
        "Digital 5 - ERRO"
    )


    Box {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                isDropDownExpanded.value = true
            }
        ) {
            Text(
                text = usernames[itemPosition.value],
                fontFamily = recursiveFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
                )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = GrayQuod,
            )
        }
        DropdownMenu(
            modifier = Modifier.background(WhiteQuod),
            expanded = isDropDownExpanded.value,
            onDismissRequest = {
                isDropDownExpanded.value = false
            }) {
            usernames.forEachIndexed { index, username ->
                DropdownMenuItem(text = {
                    Text(text = username, fontFamily = recursiveFontFamily)
                },
                    onClick = {
                        isDropDownExpanded.value = false
                        itemPosition.value = index
                        onUsernameSelected(username)
                    },
                )
            }
        }

    }
}

//@Preview(showBackground = true)
//@Composable
//fun CustomDropdownPreview() {
//    CustomDropdown()
//
//}