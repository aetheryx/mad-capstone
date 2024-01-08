package nl.hva.chatstone.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import nl.hva.chatstone.R

@Composable
fun CredentialTextField(
  value: String,
  onChanged: (String) -> Unit,
  error: String? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None
) {
  TextField(
    value = value,
    onValueChange = { onChanged(it) },
    label = { Text(stringResource(R.string.password)) },
    modifier = Modifier.fillMaxWidth(),
    colors = TextFieldDefaults.colors(),
    shape = TextFieldDefaults.shape,
    isError = error != null,
    visualTransformation = visualTransformation,
    supportingText = {
      if (error != null) {
        Text(
          error,
          color = MaterialTheme.colorScheme.error,
        )
      }
    },
    trailingIcon = {
      if (error != null) {
        Icon(
          Icons.Filled.Error,
          contentDescription = "Error",
          tint = MaterialTheme.colorScheme.error
        )
      }
    }
  )
}