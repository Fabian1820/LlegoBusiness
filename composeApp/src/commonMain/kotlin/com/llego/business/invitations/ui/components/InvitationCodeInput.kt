package com.llego.business.invitations.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun InvitationCodeInput(
    onRedeemCode: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var codeInput by remember { mutableStateOf(TextFieldValue("")) }
    var showError by remember { mutableStateOf(false) }
    
    LaunchedEffect(errorMessage) {
        showError = errorMessage != null
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Tienes un código de invitación?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = codeInput,
                onValueChange = { 
                    codeInput = it.copy(text = it.text.uppercase())
                    showError = false
                },
                modifier = Modifier.weight(1f),
                label = { Text("Código") },
                placeholder = { Text("ABC123") },
                singleLine = true,
                enabled = !isLoading,
                isError = showError,
                supportingText = if (showError && errorMessage != null) {
                    { Text(errorMessage) }
                } else null
            )
            
            Button(
                onClick = { 
                    if (codeInput.text.isNotBlank()) {
                        onRedeemCode(codeInput.text.trim())
                    }
                },
                enabled = !isLoading && codeInput.text.isNotBlank(),
                modifier = Modifier.height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Canjear")
                }
            }
        }
    }
}
