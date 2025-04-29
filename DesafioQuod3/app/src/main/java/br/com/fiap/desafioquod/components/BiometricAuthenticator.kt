package br.com.fiap.camera

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

class BiometricAuthenticator(private val context: Context) {
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val biometricManager = BiometricManager.from(context)
    private lateinit var biometricPrompt: BiometricPrompt

    fun isBiometricAuthAvailable(): BiometricAuthenticationStatus {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAuthenticationStatus.PREPARAR
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAuthenticationStatus.NAO_DISPONIVEL
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAuthenticationStatus.TEMPORARIAMENTE_NAO_DISPONIVEL
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAuthenticationStatus.DISPONIVEL_MAS_NAO_CADASTRADO
            else -> BiometricAuthenticationStatus.NAO_DISPONIVEL
        }
    }

    fun promptBiometricAuth(
        title: String,
        subTitle: String,
        negativeButtonText: String,
        fragmentActivity: FragmentActivity,
        onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
        onFailed: () -> Unit,
        onError: (errorCode: Int, errorString: String) -> Unit
    ) {
        when (isBiometricAuthAvailable()) {
            BiometricAuthenticationStatus.NAO_DISPONIVEL -> {
                onError(BiometricAuthenticationStatus.NAO_DISPONIVEL.id, "Não disponível nesse dispositivo")
                return
            }
            BiometricAuthenticationStatus.TEMPORARIAMENTE_NAO_DISPONIVEL -> {
                onError(BiometricAuthenticationStatus.TEMPORARIAMENTE_NAO_DISPONIVEL.id, "Não disponível neste momento")
                return
            }
            BiometricAuthenticationStatus.DISPONIVEL_MAS_NAO_CADASTRADO -> {
                onError(BiometricAuthenticationStatus.DISPONIVEL_MAS_NAO_CADASTRADO.id, "Você precisa cadastrar primeiro sua digital")
                return
            }
            else -> Unit
        }

        biometricPrompt = BiometricPrompt(fragmentActivity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subTitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
