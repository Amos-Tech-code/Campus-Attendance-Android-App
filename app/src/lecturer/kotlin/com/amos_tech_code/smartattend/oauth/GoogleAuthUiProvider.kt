package com.amos_tech_code.smartattend.oauth

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.amos_tech_code.smartattend.domain.models.GoogleAccount
import com.amos_tech_code.smartattend.utils.GoogleServerClientId
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential


class GoogleAuthUiProvider {

    suspend fun signIn(
        activityContext: Context,
        credentialManager: CredentialManager
    ) : GoogleAccount {

        val credentials = credentialManager.getCredential(
            activityContext,
            getCredentialRequest()
        ).credential

        return handleCredentials(credentials)
    }


    private fun handleCredentials(credentials: Credential): GoogleAccount {
        when {
            credentials is CustomCredential && credentials.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleIdTokenCredential = credentials as GoogleIdTokenCredential
                return GoogleAccount(
                    token = googleIdTokenCredential.idToken,
                    profileName = googleIdTokenCredential.displayName ?: "",
                    profileImageUrl = googleIdTokenCredential.profilePictureUri.toString()
                )
            }

            else -> {
                throw IllegalStateException("Invalid credential type")
            }
        }
    }


    private fun getCredentialRequest(): GetCredentialRequest {
        return GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(
                    GoogleServerClientId
                ).build()
            ).build()
    }
}