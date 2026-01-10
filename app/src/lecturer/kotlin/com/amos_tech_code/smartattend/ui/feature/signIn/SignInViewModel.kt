package com.amos_tech_code.smartattend.ui.feature.signIn

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amos_tech_code.smartattend.data.local.shared_prefs.ClassTrackProSession
import com.amos_tech_code.smartattend.data.network.utils.ApiError
import com.amos_tech_code.smartattend.data.network.utils.ApiResult
import com.amos_tech_code.smartattend.data.repositories.AuthRepository
import com.amos_tech_code.smartattend.oauth.GoogleAuthUiProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val repository: AuthRepository,
    private val session: ClassTrackProSession
) : ViewModel() {

    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)
    val state: StateFlow<SignInState> = _state

    private val _event = Channel<SignInEvent>()
    val event = _event.receiveAsFlow()

    var googleAuthUiLoading = mutableStateOf(false); private set

    private val googleAuthUiProvider = GoogleAuthUiProvider()

    fun initiateGoogleLogin(context: ComponentActivity) {
        viewModelScope.launch {
            googleAuthUiLoading.value = true
            try {
                val response = googleAuthUiProvider.signIn(
                    context,
                    CredentialManager.create(context)
                )
                //Log.d("GoogleSignIn", response.toString())
                onGoogleLoginSuccess(response.token)
                googleAuthUiLoading.value = false

            } catch (e: Exception) {
                //Log.e("GoogleSignIn", "Sign-in failed", e)
                googleAuthUiLoading.value = false
                when (e) {
                    is GetCredentialCancellationException -> {
                        _event.send(SignInEvent.ShowErrorDialog("Sign-in cancelled."))
                    }
                    else -> {
                        _event.send(SignInEvent.ShowErrorDialog("Something went wrong. Please try again."))
                    }
                }
            }

        }
    }

    fun onGoogleLoginSuccess(token: String) {
        _state.value = SignInState.Loading
        viewModelScope.launch {

            try {
                when (val result = repository.googleSignIn(token)) {
                    is ApiResult.Success -> {
                        session.saveLecturerSession(
                            token = result.data.token,
                            name = result.data.name,
                            email = result.data.email,
                            isProfileComplete = result.data.profileComplete
                        )
                        _state.value = SignInState.Success
                        _event.send(SignInEvent.NavigateToHome)

                    }
                    is ApiResult.Failure -> {
                        _state.value = SignInState.Error
                        when (val error = result.error) {
                            is ApiError.HttpError -> {
                                _event.send(SignInEvent.ShowErrorDialog(error.message))
                            }
                            is ApiError.NetworkError -> {
                                _event.send(SignInEvent.ShowNetworkErrorDialog(error.exception.message ?: "Network Error. Please check your internet connection and try again."))
                            }
                            is ApiError.UnknownError -> {
                                _event.send(SignInEvent.ShowErrorDialog(error.throwable.message ?: "An Unknown error occurred. Please try again later."))
                            }

                        }
                    }
                }

            } catch (e: Exception) {
                _state.value = SignInState.Error
                _event.send(SignInEvent.ShowErrorDialog(e.message ?: "An Unknown error occurred. Please try again later."))
            }
        }
    }


}