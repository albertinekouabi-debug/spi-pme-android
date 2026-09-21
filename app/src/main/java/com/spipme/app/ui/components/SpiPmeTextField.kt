package com.spipme.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun SpiPmeTextField(
    valeur: String,
    surChangement: (String) -> Unit,
    libelle: String,
    modifier: Modifier = Modifier,
    iconeDebut: (@Composable () -> Unit)? = null,
    motDePasse: Boolean = false,
    messageErreur: String? = null,
    typeClavier: KeyboardType = KeyboardType.Text,
) {
    var motDePasseVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    OutlinedTextField(
        value = valeur,
        onValueChange = surChangement,
        modifier = modifier.fillMaxWidth(),
        label = { Text(libelle) },
        leadingIcon = iconeDebut,
        trailingIcon = if (motDePasse) {
            {
                IconButton(onClick = { motDePasseVisible = !motDePasseVisible }) {
                    Icon(
                        imageVector = if (motDePasseVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (motDePasseVisible) "Masquer le mot de passe" else "Afficher le mot de passe",
                    )
                }
            }
        } else null,
        visualTransformation = if (motDePasse && !motDePasseVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (motDePasse) KeyboardType.Password else typeClavier,
        ),
        isError = messageErreur != null,
        supportingText = messageErreur?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}
