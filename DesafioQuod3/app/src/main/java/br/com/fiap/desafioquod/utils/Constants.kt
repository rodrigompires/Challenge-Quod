package br.com.fiap.desafioquod.utils

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

// Mensagens de Feedback
val FEEDBACK_MANTENHA_POSICAO = "Mantenha a posição!"
val FEEDBACK_POSICIONE_SEU_ROSTO = "Posicione seu rosto"
val FEEDBACK_MOVIMENTO_DETECTADO = "Detectando movimento ou piscar. Tente mover a cabeça ou piscar os olhos."
val FEEDBACK_APROXIME_SEU_ROSTO = "Aproxime seu rosto"
val FEEDBACK_FACE_PROXIMA = "Afaste o seu rosto"
val CAPTURE_DELAY = 1000L

// Restriçoes Face
val MIN_FACE_WIDTH = 330
val MAX_FACE_WIDTH = 620
val LIVENESS_EYE_THRESHOLD = 0.3f
val LIVENESS_HEAD_MOVEMENT_THRESHOLD = -5f..5f

// Restriçoes retangulo
val RECTANGLE_WIDTH_MIN = 510
val RECTANGLE_WIDTH_MAX = 710


// Variavel para estilizar o texto
val textLineHeight = TextStyle(lineHeight = 20.sp)

val textDidYouKnow = "Você Sabia?"


// Variáveis para os textos - Facial Biometric
//------------------------------------------------------------------------

val explanatoryText_1 = "Vamos utilizar uma ferramenta de reconhecimento facial. \nGaranta que seu rosto esteja bem iluminado, " +
        "retire óculos escuros ou máscaras."
val explanatoryText_2 = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append("Para garantir a precisão da sua biometria facial, vamos capturar duas fotos e permissões de acesso à:\n\n")
    }

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append("\uD83D\uDE10 ")
    }
    append("A primeira foto será com o seu rosto em uma expressão neutra, sem sorrir.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append("\uD83D\uDE0A ") // Emoji de rosto sorrindo
    }
    append("A segunda foto será com um sorriso, para verificar diferentes expressões faciais.\n")
}
val explanatoryText_3 = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append("✓ Câmera: ")
    }
    append("para capturar sua imagem.\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append("✓ Localização em tempo real: ")
    }
    append("para verificar sua presença.\n\n")

    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append("Por favor, conceda as permissões necessárias.")
    }
}

val explanatoryText_4 = "A biometria facial é uma tecnologia que utiliza as características únicas do rosto de uma pessoa para " +
        "identificá-la. \nAtravés de softwares, é possível comparar imagens faciais e validar suas informações."




val facialFraudMessage = "Possível Fraude Detectada! \n\nFalha na validação das imagens: \n\nImagens faciais não similares."
val successMessageFacial = "Imagens faciais validadas com sucesso!"
val coordinatesFacialFraudMessage = "Possível Fraude Detectada! \n\nFalha na validação das imagens: \nCoordenadas diferentes."
val facialCoordinatesFacialFraudMessage = "Possível Fraude Detectada! \n\nFalha na validação das imagens: \nFaces e Coordenadas diferentes."


// Variáveis para os textos - Documentoscopy
//------------------------------------------------------------------------

val dialogTextDocumetcospy = "A documentoscopia é a ciência que examina documentos para descobrir sua verdadeira origem e " +
        "identificar qualquer alteração ou fraude. " +
        "Suas informações serão criptografadas e armazenadas de forma segura."

val explanatoryTextDoc_1 = "Vamos utilizar uma ferramenta de scanner digital. Capture a FRENTE e o VERSO do documento escolhido."
val explanatoryTextDoc_2 = "Escolha uma das opções abaixos para escolher qual documento será digitalizado.\n"
val explanatoryTextDoc_3 = "As imagens abaixo estão nítidas?\n"

val successMessageDoc = "Documentos validados com sucesso!"
val coordinatesFraudMessage = "Possível Fraude Detectada! \n\nFalha na validação dos documentos: \nCoordenadas discrepantes."
val docFraudMessage = "Possível Fraude Detectada! \n\nFalha na validação dos documentos: Não é um "
val docCoordinatesFraudMessage = "Possível Fraude Detectada! \n\nFalha na validação dos documentos: Coordenadas e Documento discrepantes, não é um "
val docErrorFraudMessage = "Possível Fraude Detectada! \n\nFalha na validação dos documentos: Esperado , identificado como ."
val docErrorMessage = "Falha na validação dos documentos. "
val failedDocMessage = "Falha na validação dos documentos: Coordenadas inválidas."


// Variáveis para os textos - Digital
//------------------------------------------------------------------------

val textInitial =
    "Um modo rápido e fácil de validar-se nos aplicativos.\nSeus dados de impressão digital, são protegidos.\nUtilizaremos o leitor digital de seu aparelho para capturar sua impressão digital!"

val explanatoryTextBiom_1 = "A biometria digital é como uma \"senha pessoal\" única, baseada em características físicas suas, como impressões digitais, rosto ou íris. Ela serve para te identificar de forma segura e rápida, substituindo senhas tradicionais. Seus dados biométricos são transformados em códigos e guardados com segurança, protegendo sua identidade."
val message_1 = "Não foi localizada uma digital cadastrada. Por favor, verifique e cadastre uma digital."
val successMessage = "Autenticação biométrica concluída com sucesso!"
val failureMessage = "Falha na autenticação biométrica. Tente novamente."
val fraudDetectedMessage = "Possível fraude detectada! \n\nDigital não reconhecida! \nMuitas tentativas falhas! \nTente novamente!"
val serverErrorMessage = "Erro no servidor. Tente novamente."
val connectionErrorMessage = "Erro de conexão. Verifique sua internet."
val errorMessage = "Erro desconhecido. Tente novamente."