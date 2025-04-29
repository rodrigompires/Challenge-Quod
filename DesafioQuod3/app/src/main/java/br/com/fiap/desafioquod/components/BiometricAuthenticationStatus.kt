package br.com.fiap.camera

// Define uma enum class chamada BiometricAuthenticationStatus, que representa os diferentes estados da autenticação biométrica.
enum class BiometricAuthenticationStatus (val id: Int) {
    // PREPARAR: Estado em que a autenticação biométrica está disponível e pronta para ser usada.
    PREPARAR(1),
    // NAO_DISPONIVEL: Estado em que a autenticação biométrica não está disponível no dispositivo.
    NAO_DISPONIVEL(-1),
    // TEMPORARIAMENTE_NAO_DISPONIVEL: Estado em que a autenticação biométrica está temporariamente indisponível.
    TEMPORARIAMENTE_NAO_DISPONIVEL(-2),
    // DISPONIVEL_MAS_NAO_CADASTRADO: Estado em que a autenticação biométrica está disponível, mas o usuário não cadastrou nenhuma biometria.
    DISPONIVEL_MAS_NAO_CADASTRADO(-3)
}