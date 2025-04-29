package br.com.fiap.challenge_quod.challenge_quod.service;

import br.com.fiap.challenge_quod.challenge_quod.dto.DocumentRequestDTO;
import br.com.fiap.challenge_quod.challenge_quod.dto.DocumentResponseDTO;
import br.com.fiap.challenge_quod.challenge_quod.model.Device;
import br.com.fiap.challenge_quod.challenge_quod.model.IdentityDocument;
import br.com.fiap.challenge_quod.challenge_quod.model.Metadata;
import br.com.fiap.challenge_quod.challenge_quod.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.opencv.core.*;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private static final String LIBS_PATH = System.getProperty("user.dir") + File.separator + "libs" + File.separator;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    private static final String FRAUD_API_URL = "https://challenge-quod-fraudes.free.beeceptor.com";
    // Variavel para teste forçado de fraude
    private static final boolean FORCE_FRAUD_TEST = false;

    @Autowired
    private DocumentRepository documentRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentService() {
        String libPath = LIBS_PATH + "opencv_java411.dll";
        File dllFile = new File(libPath);
        logger.info("Tentando carregar OpenCV de: {}", libPath);
        if (!dllFile.exists()) {
            logger.error("Arquivo opencv_java411.dll não encontrado em: {}", libPath);
            throw new RuntimeException("Arquivo opencv_java411.dll não encontrado em: " + libPath);
        }
        System.load(libPath);
        logger.info("OpenCV carregado com sucesso. Versão: {}", Core.getVersionString());

        File uploadsDir = new File(UPLOAD_DIR);
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
            logger.info("Pasta uploads criada em: {}", UPLOAD_DIR);
        }
    }

    public DocumentResponseDTO validateAndSaveDocument(DocumentRequestDTO requestDTO) {
        logger.info("Iniciando validação e salvamento para documentType: {}", requestDTO.getDocumentType());
        String resultadoValidacao = validateDocument(requestDTO);
        logger.info("Resultado da validação: {}", resultadoValidacao);

        Double docScore;
        switch (resultadoValidacao) {
            case "Válido - Alta confiança":
                docScore = 0.95;
                break;
            case "Válido - Média confiança":
                docScore = 0.75;
                break;
            case "Válido - Baixa confiança":
                docScore = 0.50;
                break;
            default:
                docScore = 0.0;
                break;
        }
        logger.info("DocScore atribuído: {}", docScore);

        Double lat1 = requestDTO.getLatitudeFront();
        Double lon1 = requestDTO.getLongitudeFront();
        Double lat2 = requestDTO.getLatitudeBack();
        Double lon2 = requestDTO.getLongitudeBack();

        String tipoFraude = determinarTipoFraude(resultadoValidacao, requestDTO.getDocumentType(), lat1, lon1, lat2, lon2);
        logger.info("Tipo de fraude determinado: {}", tipoFraude);

        if (FORCE_FRAUD_TEST) {
            lat2 = (lat2 != null ? lat2 : 0.0) + 1.0;
            lon2 = (lon2 != null ? lon2 : 0.0) + 1.0;
            logger.debug("FORCE_FRAUD_TEST ativado: Coordenadas ajustadas - lat2: {}, lon2: {}", lat2, lon2);
            tipoFraude = determinarTipoFraude(resultadoValidacao, requestDTO.getDocumentType(), lat1, lon1, lat2, lon2);
            logger.info("Tipo de fraude reavaliado após ajuste: {}", tipoFraude);
        }

        String message = tipoFraude;

        String fabricante = requestDTO.getManufacturer() != null ? requestDTO.getManufacturer().replace("\"", "") : "unknown";
        String modelo = requestDTO.getModel() != null ? requestDTO.getModel().replace("\"", "") : "unknown";
        String androidVersion = requestDTO.getAndroidVersion() != null ? "Android " + requestDTO.getAndroidVersion() : "unknown";

        Device dispositivo = new Device(fabricante, modelo, androidVersion);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ipOrigem = request.getRemoteAddr();

        Metadata metadadosImagem1 = createMetadata(requestDTO.getFrontImage(), lat1, lon1, ipOrigem);
        Metadata metadadosImagem2 = createMetadata(requestDTO.getBackImage(), lat2, lon2, ipOrigem);

        ZonedDateTime now = ZonedDateTime.now();
        String baseDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'_T_'HH:mm:ss"));
        String micros = String.format("%07d", now.getNano() / 100);
        String dataCaptura24h = baseDateTime + "._" + micros + now.format(DateTimeFormatter.ofPattern("XXX"));

        IdentityDocument document = new IdentityDocument(
                String.valueOf(System.currentTimeMillis()),
                "Documentoscopia",
                tipoFraude,
                requestDTO.getCaptureDate() != null ? now.format(DateTimeFormatter.ISO_INSTANT) : now.format(DateTimeFormatter.ISO_INSTANT),
                dataCaptura24h,
                dispositivo,
                Arrays.asList("sms", "email"),
                "Sistema Anti Fraude",
                metadadosImagem1,
                metadadosImagem2
        );

        if ("Sem Fraude".equals(tipoFraude)) {
            document.setFraudeStatus(1);
        } else {
            document.setFraudeStatus(2);
        }

        IdentityDocument savedDocument = documentRepository.save(document);
        logger.info("Documento salvo com transacaoId: {} e tipoFraude: {}", savedDocument.getTransacaoId(), savedDocument.getTipoFraude());

        if (!"Sem Fraude".equals(tipoFraude)) {
            sendToExternalApi(savedDocument);
        } else {
            logger.info("Nenhuma fraude detectada (tipoFraude=Sem Fraude). Nenhuma notificação enviada à API externa.");
        }

        return new DocumentResponseDTO(
                savedDocument.getTransacaoId(),
                savedDocument.getTipoBiometria(),
                message,
                docScore
        );
    }

    private void sendToExternalApi(IdentityDocument savedDocument) {
        try {
            String json = objectMapper.writeValueAsString(savedDocument);
            logger.info("Fraude detectada. Enviando JSON para a API externa: {}", json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FRAUD_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.warn(">>> RESPOSTA DA API EXTERNA <<< Status: {}, Body: {}", response.statusCode(), response.body());
        } catch (Exception e) {
            logger.error("Erro ao enviar para a API externa: {}", e.getMessage(), e);
        }
    }

    private String determinarTipoFraude(String resultadoValidacao, String documentType, Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            logger.debug("Coordenadas ausentes: lat1={}, lon1={}, lat2={}, lon2={}", lat1, lon1, lat2, lon2);
            return "Coordenadas Inválidas";
        }

        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        double tolerance = 20.0;
        boolean coordenadasProximas = distance <= tolerance;

        logger.debug("Distância entre coordenadas: {:.2f} metros (lat1: {}, lon1: {}, lat2: {}, lon2: {})", distance, lat1, lon1, lat2, lon2);
        logger.debug("Coordenadas próximas (tolerância {}m): {}", tolerance, coordenadasProximas);

        boolean documentoValido = resultadoValidacao.equals("Válido") ||
                resultadoValidacao.equals("Válido - Alta confiança") ||
                resultadoValidacao.equals("Válido - Média confiança");
        boolean documentoIncorreto = resultadoValidacao.contains("Documento é");

        logger.debug("Documento válido para {}: {}", documentType, documentoValido);
        logger.debug("Documento incorreto identificado: {}", documentoIncorreto);

        if (documentoValido && coordenadasProximas) {
            return "Sem Fraude";
        } else if (documentoValido && !coordenadasProximas) {
            return "Coordenadas Discrepantes";
        } else if (documentoIncorreto) {
            String tipoIdentificado = resultadoValidacao.split("Documento é ")[1].split(",")[0];
            return String.format("Documento incorreto - Esperado %s, identificado como %s", documentType, tipoIdentificado);
        } else if (!documentoValido && coordenadasProximas) {
            return String.format("Documento inválido - Não é um %s", documentType);
        } else {
                return String.format("Coordenadas e Documento discrepantes - Não é um %s", documentType);
        }
    }

    private String validateDocument(DocumentRequestDTO requestDTO) {
        logger.info("Validando documento do tipo: {}", requestDTO.getDocumentType());
        if (requestDTO.getFrontImage() == null || (requestDTO.getDocumentType().equalsIgnoreCase("RG") && requestDTO.getBackImage() == null) ||
                (requestDTO.getDocumentType().equalsIgnoreCase("CNH") && requestDTO.getBackImage() == null)) {
            logger.warn("Imagens ausentes para validação");
            return "Inválido - Imagens ausentes";
        }

        try {
            File frontFile = convertMultipartFileToFile(requestDTO.getFrontImage());
            File backFile = requestDTO.getDocumentType().equalsIgnoreCase("CPF") ? null : convertMultipartFileToFile(requestDTO.getBackImage());
            logger.debug("Arquivos temporários criados - Frente: {}, Verso: {}", frontFile.getAbsolutePath(), backFile != null ? backFile.getAbsolutePath() : "N/A");

            String documentType = requestDTO.getDocumentType().replace("\"", "").trim().toUpperCase();
            logger.info("DocumentType limpo para validação: {}", documentType);

            return switch (documentType) {
                case "RG" -> validateRG(frontFile, backFile);
                case "CPF" -> validateCPF(frontFile, backFile);
                case "CNH" -> validateCNH(frontFile, backFile);
                default -> {
                    logger.warn("Tipo de documento desconhecido após limpeza: {}", documentType);
                    yield "Inválido - Tipo de documento desconhecido";
                }
            };
        } catch (IOException e) {
            logger.error("Erro ao processar documento: {}", e.getMessage(), e);
            return "Erro ao processar documento: " + e.getMessage();
        }
    }

    //    FUNÇÕES DE VALIDAÇÃO DO RG
    //    -------------------------------------------------------------------------------
    private String validateRG(File frontFile, File backFile) {
        logger.info("Validando se a imagem é de um RG brasileiro");

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            logger.info("Diretório de uploads criado: {}", UPLOAD_DIR);
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String frontSavedPath = UPLOAD_DIR + "front_" + timestamp + ".jpg";
        String backSavedPath = backFile != null ? UPLOAD_DIR + "back_" + timestamp + ".jpg" : null;

        Mat frontImage = Imgcodecs.imread(frontFile.getAbsolutePath());
        if (frontImage.empty()) {
            logger.error("Erro ao carregar imagem da frente");
            return "Erro - Imagem não carregada";
        }

        logImageColorInfoRG(frontImage);
        Imgcodecs.imwrite(frontSavedPath, frontImage);
        logger.info("Imagem salva - Frente: '{}'", frontSavedPath);

        Mat backImage = null;
        if (backFile != null) {
            backImage = Imgcodecs.imread(backFile.getAbsolutePath());
            if (backImage.empty()) {
                logger.warn("Erro ao carregar imagem do verso");
                backFile = null;
            } else {
                Imgcodecs.imwrite(backSavedPath, backImage);
                logger.info("Imagem salva - Verso: '{}'", backSavedPath);
            }
        }

        // Configuração do Tesseract
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(LIBS_PATH);
        tesseract.setLanguage("por");
        tesseract.setPageSegMode(3);
        tesseract.setOcrEngineMode(1);
        tesseract.setTessVariable("preserve_interword_spaces", "1");

        String frontText = "";
        try {
            frontText = tesseract.doOCR(frontFile);
            logger.debug("Texto extraído da frente (original): {}", frontText);
        } catch (TesseractException e) {
            logger.error("Erro ao realizar OCR na frente: {}", e.getMessage());
        }

        String backText = "";
        if (backFile != null) {
            try {
                backText = tesseract.doOCR(backFile);
                logger.debug("Texto extraído do verso (original): {}", backText);
            } catch (TesseractException e) {
                logger.error("Erro ao realizar OCR no verso: {}", e.getMessage());
            }
        }

        String extractedText = frontText + "\n" + backText;
        logger.debug("Texto combinado (frente + verso): {}", extractedText);

        String textLower = extractedText.toLowerCase();
        String[] cnhKeywords = {
                "trânsito", "habilita[çc][ãa]o", "detran", "carteira\\s+nacional",
                "permiss[ãa]o", "categoria\\s+[abcde]", "válida\\s+em\\s+todo",
                "infraestrutura", "dirigir", "renach"
        };

        for (String pattern : cnhKeywords) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(textLower).find()) {
                logger.warn("Indicador de CNH encontrado: '{}'. Documento não é um RG.", pattern);
                return "Inválido - Documento identificado como CNH em vez de RG";
            }
        }

        String documentType = identifyDocumentType(extractedText);
        logger.info("Documento identificado como: {}", documentType);

        if (!documentType.equals("RG") && !documentType.equals("Indeterminado")) {
            logger.warn("Documento identificado como {}. Não é um RG.", documentType);
            return "Inválido - Documento identificado como " + documentType + " em vez de RG";
        }

        Map<String, Integer> contradictionIndicators = checkContradictoryIndicators(extractedText);
        if (!contradictionIndicators.isEmpty()) {
            String contradictions = contradictionIndicators.entrySet().stream()
                    .map(e -> e.getKey() + " (" + e.getValue() + " ocorrências)")
                    .collect(Collectors.joining(", "));
            logger.info("Documento contém indicadores contraditórios: {}", contradictions);
            return "Inválido - Contém indicadores de " +
                    contradictionIndicators.keySet().stream().collect(Collectors.joining(" e "));
        }

        boolean hasCorrectColor = hasRGColorPattern(frontImage);
        double colorSimilarity = calculateRGColorSimilarity(frontImage);
        boolean hasContrastText = hasHighContrastTextRG(frontImage);
        boolean hasRGDims = hasRGDimensions(frontImage);

        int score = 0;

        if (hasCorrectColor) {
            score += 6;
            logger.debug("Documento possui cor característica do RG: +6 pontos");
        }
        if (colorSimilarity > 0.1) {
            int colorPoints = Math.min(3, (int)(colorSimilarity * 15));
            score += colorPoints;
            logger.debug("Similaridade de cor com o padrão do RG: {}%, +{} pontos",
                    Math.round(colorSimilarity * 100), colorPoints);
        }
        if (hasContrastText) {
            score += 1;
            logger.debug("Documento possui contraste típico de texto: +1 ponto");
        }
        if (hasRGDims) {
            score += 1;
            logger.debug("Documento possui dimensões retangulares comuns: +1 ponto");
        }

        Map<String, Integer> rgIndicators = evaluateRGTextualIndicators(extractedText);
        for (Map.Entry<String, Integer> indicator : rgIndicators.entrySet()) {
            score += indicator.getValue();
            logger.debug("{}: +{} pontos", indicator.getKey(), indicator.getValue());
        }

        logger.info("Pontuação total da verificação: {}", score);

        boolean hasStrongRGIndicator = rgIndicators.containsKey("'Cédula de Identidade' ou 'Registro Geral'");
        if (!hasStrongRGIndicator && score < 15) {
            logger.info("Documento não possui indicadores fortes de RG e pontuação insuficiente: {}", score);
            return "Inválido - Não aparenta ser um RG brasileiro";
        }

        if (score >= 18) {
            logger.info("Documento validado como Válido (Alta confiança) - Pontuação: {}", score);
            return "Válido - Alta confiança";
        } else if (score >= 13) {
            logger.info("Documento validado como Válido (Média confiança) - Pontuação: {}", score);
            return "Válido - Média confiança";
        } else if (score >= 8) {
            logger.info("Documento validado como Válido (Baixa confiança) - Pontuação: {}", score);
            return "Válido - Baixa confiança";
        } else {
            logger.info("Documento validado como Inválido - Não aparenta ser um RG - Pontuação: {}", score);
            return "Inválido - Não aparenta ser um RG brasileiro";
        }
    }

    private void logImageColorInfoRG(Mat image) {
        // Converter para o espaço de cor HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

        // Calcular a cor média da imagem
        Scalar meanColor = Core.mean(image);

        // Extrair componentes BGR
        double meanBlue = meanColor.val[0];
        double meanGreen = meanColor.val[1];
        double meanRed = meanColor.val[2];

        // Calcular média HSV
        Scalar meanHSV = Core.mean(hsv);
        double meanHue = meanHSV.val[0];
        double meanSaturation = meanHSV.val[1];
        double meanValue = meanHSV.val[2];

        // Calcular predominância de cinza
        double maxChannel = Math.max(Math.max(meanRed, meanGreen), meanBlue);
        double minChannel = Math.min(Math.min(meanRed, meanGreen), meanBlue);
        double colorDeviation = (maxChannel - minChannel) / 255.0;
        double grayRatio = 1.0 - colorDeviation;

        // Criar representação hexadecimal da cor média
        String hexColor = String.format("#%02X%02X%02X",
                (int)meanRed,
                (int)meanGreen,
                (int)meanBlue);

        // Logar informações detalhadas
        logger.info("Análise de cor da imagem:");
        logger.info("Cor média (BGR): B={}, G={}, R={}", meanBlue, meanGreen, meanRed);
        logger.info("Cor média (Hex): {}", hexColor);
        logger.info("Valores HSV: H={} ({}°), S={}%, V={}%",
                meanHue,
                Math.round(meanHue * 2),
                Math.round(meanSaturation / 255 * 100),
                Math.round(meanValue / 255 * 100));
        logger.info("Predominância de cinza: {}%", Math.round(grayRatio * 100));

        // Verificar se a cor predominante é próxima da cor padrão do RG
        double rgColorProximity = calculateRGColorProximity(meanRed, meanGreen, meanBlue);
        logger.info("Proximidade com cor padrão do RG (#ABADA0 - #ACB1AB): {}%",
                Math.round(rgColorProximity * 100));
    }

    private double calculateRGColorProximity(double r, double g, double b) {
        // Cores padrão do RG: #ABADA0 e #ACB1AB
        // #ABADA0 = RGB(171, 173, 160)
        // #ACB1AB = RGB(172, 177, 171)
        double rgR1 = 171;
        double rgG1 = 173;
        double rgB1 = 160;

        double rgR2 = 172;
        double rgG2 = 177;
        double rgB2 = 171;

        // Calcular distância para ambas as cores
        double distance1 = Math.sqrt(
                Math.pow((r - rgR1) / 255, 2) +
                        Math.pow((g - rgG1) / 255, 2) +
                        Math.pow((b - rgB1) / 255, 2)
        );

        double distance2 = Math.sqrt(
                Math.pow((r - rgR2) / 255, 2) +
                        Math.pow((g - rgG2) / 255, 2) +
                        Math.pow((b - rgB2) / 255, 2)
        );

        // Usar a menor distância
        double minDistance = Math.min(distance1, distance2);
        return Math.max(0, 1 - (minDistance / Math.sqrt(3)));
    }

    private String identifyDocumentType(String text) {
        if (text == null || text.isEmpty()) {
            return "Indeterminado";
        }

        String textLower = text.toLowerCase();

        Map<String, List<String>> strongIndicators = new HashMap<>();
        Map<String, List<String>> weakIndicators = new HashMap<>();

        strongIndicators.put("CNH", Arrays.asList(
                "trânsito", "habilita[çc][ãa]o", "detran", "carteira\\s+nacional",
                "permiss[ãa]o", "categoria\\s+[abcde]", "válida\\s+em\\s+todo",
                "infraestrutura", "dirigir", "renach"
        ));

        // CNH - Indicadores fracos
        weakIndicators.put("CNH", Arrays.asList(
                "\\bcnh\\b", "\\bacc\\b", "validade", "registro", "\\b[0-9]{11}\\b"
        ));

        // RG - Indicadores fortes
        strongIndicators.put("RG", Arrays.asList(
                "c[eé]dula\\s+de\\s+identidade",
                "registro\\s+geral",
                "secretaria\\s+de\\s+segurança\\s+p[úu]blica"
        ));

        weakIndicators.put("RG", Arrays.asList(
                "\\brg\\b", "identidade", "\\bssp\\b"
        ));

        for (Map.Entry<String, List<String>> entry : strongIndicators.entrySet()) {
            String docType = entry.getKey();
            List<String> patterns = entry.getValue();

            for (String pattern : patterns) {
                if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(textLower).find()) {
                    logger.debug("Indicador forte de {} encontrado: '{}'", docType, pattern);
                    return docType;
                }
            }
        }

        Map<String, Integer> weakMatches = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : weakIndicators.entrySet()) {
            String docType = entry.getKey();
            List<String> patterns = entry.getValue();

            int matchCount = 0;
            List<String> matchedPatterns = new ArrayList<>();

            for (String pattern : patterns) {
                if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(textLower).find()) {
                    matchCount++;
                    matchedPatterns.add(pattern);
                }
            }

            if (matchCount > 0) {
                logger.debug("Encontrados {} indicadores fracos de {}: {}", matchCount, docType, matchedPatterns);
                weakMatches.put(docType, matchCount);
            }
        }

        Integer cnhCount = weakMatches.getOrDefault("CNH", 0);
        Integer rgCount = weakMatches.getOrDefault("RG", 0);
        if (cnhCount >= 2 && cnhCount >= rgCount) {
            return "CNH";
        } else if (rgCount >= 2) {
            return "RG";
        }

        return "Indeterminado";
    }

    private Map<String, Integer> checkContradictoryIndicators(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyMap();
        }

        String textLower = text.toLowerCase();
        Map<String, Integer> contradictions = new HashMap<>();

        String[] cnhContradictions = {
                "trânsito", "habilita[çc][ãa]o", "detran", "carteira\\s+nacional",
                "permiss[ãa]o", "categoria\\s+[abcde]", "válida\\s+em\\s+todo",
                "infraestrutura", "dirigir", "renach"
        };

        int cnhCount = 0;
        for (String pattern : cnhContradictions) {
            Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(textLower);
            while (matcher.find()) {
                cnhCount++;
                logger.debug("Indicador contraditório de CNH encontrado: '{}'", pattern);
            }
        }

        if (cnhCount > 0) {
            contradictions.put("CNH", cnhCount);
        }

        return contradictions;
    }

    private Map<String, Integer> evaluateRGTextualIndicators(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> indicators = new LinkedHashMap<>();
        String textLower = text.toLowerCase();

        boolean hasRGText = Pattern.compile("c[eé]dula\\s+de\\s+identidade|registro\\s+geral",
                Pattern.CASE_INSENSITIVE).matcher(textLower).find();
        if (hasRGText) {
            indicators.put("'Cédula de Identidade' ou 'Registro Geral'", 8);
        }

        boolean hasSSP = Pattern.compile("ssp|secretaria\\s*(da|de)?\\s*seguran[çc]a",
                Pattern.CASE_INSENSITIVE).matcher(textLower).find();
        if (hasSSP) {
            indicators.put("Referência a 'SSP' ou similar", 4);
        }

        boolean hasRgNumber = Pattern.compile("\\d{1,2}[\\.\\s]?\\d{3}[\\.\\s]?\\d{3}[-0-9A-Za-z]?",
                Pattern.CASE_INSENSITIVE).matcher(text).find();
        if (hasRgNumber) {
            indicators.put("Número de RG", 3);
        }

        boolean hasRepublicaFederativa = Pattern.compile("rep[uú]blica\\s*federativa\\s*(do\\s+)?brasil",
                Pattern.CASE_INSENSITIVE).matcher(textLower).find();
        if (hasRepublicaFederativa) {
            indicators.put("'República Federativa do Brasil'", 2);
        }

        boolean hasFiliacao = Pattern.compile("filia[çc][ãa]o|filho(a)?\\s*(de)?|pai|m[ãa]e",
                Pattern.CASE_INSENSITIVE).matcher(textLower).find();
        if (hasFiliacao) {
            indicators.put("Filiação encontrada", 3);
        }

        boolean hasDataNascimento = Pattern.compile("\\d{2}/\\d{2}/\\d{4}",
                Pattern.CASE_INSENSITIVE).matcher(textLower).find();
        if (hasDataNascimento) {
            indicators.put("Data de nascimento", 2);
        }

        return indicators;
    }

    private boolean isRGCentralText(String text) {
        Pattern centralRGPattern = Pattern.compile("^\\s*rg\\s*$|^\\s*r\\.?g\\.?\\s*$|^\\s*registro\\s+geral\\s*$|^\\s*c[eé]dula\\s+de\\s+identidade\\s*$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        return centralRGPattern.matcher(text).find();
    }

    private boolean isRGNumberProminent(String text) {
        // Padrão para um número de RG (formatos comuns no Brasil)
        Pattern prominentRGPattern = Pattern.compile("^\\s*\\d{1,3}\\.\\d{3}\\.\\d{3}(-[0-9A-Za-z])?\\s*$|^\\s*\\d{6,9}(-[0-9A-Za-z])?\\s*$",
                Pattern.MULTILINE);
        return prominentRGPattern.matcher(text).find();
    }

    private boolean hasRGColorPattern(Mat image) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

        // Faixa ajustada para incluir H=49, S=9%, V=61%
        Scalar lowerGray = new Scalar(30, 0, 50);  // Ampliado para tons verdes/cinzas
        Scalar upperGray = new Scalar(120, 50, 200); // Inclui variações de RG

        Mat grayMask = new Mat();
        Core.inRange(hsv, lowerGray, upperGray, grayMask);

        double grayPixels = Core.countNonZero(grayMask);
        double totalPixels = image.rows() * image.cols();
        double grayRatio = grayPixels / totalPixels;

        logger.debug("Proporção de pixels na cor do RG na imagem: {}%", Math.round(grayRatio * 100));
        return grayRatio > 0.15; // Reduzido de 0.25
    }

    private double calculateRGColorSimilarity(Mat image) {
        Mat resized = new Mat();
        if (image.width() > 800 || image.height() > 800) {
            Size newSize = new Size(image.width() / 2, image.height() / 2);
            Imgproc.resize(image, resized, newSize);
        } else {
            resized = image.clone();
        }

        Mat hsv = new Mat();
        Imgproc.cvtColor(resized, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar rgColor1HSV = new Scalar(49, 23, 155); // Ajustado para #979892 (aproximado)
        Scalar lowerBound1 = new Scalar(30, 0, 50);
        Scalar upperBound1 = new Scalar(70, 50, 200);

        Mat mask1 = new Mat();
        Core.inRange(hsv, lowerBound1, upperBound1, mask1);

        double similarPixels = Core.countNonZero(mask1);
        double totalPixels = mask1.total();
        double similarity = similarPixels / totalPixels;

        logger.debug("Similaridade de cor com as cores padrão do RG: {}%", Math.round(similarity * 100));
        return similarity;
    }

    private boolean hasHighContrastTextRG(Mat image) {
        // Converter para escala de cinza
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Aplicar limiarização adaptativa para destacar texto
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(gray, thresh, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV, 11, 2);

        // Encontrar componentes conectados (potenciais caracteres)
        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        int connectedComponents = Imgproc.connectedComponentsWithStats(
                thresh, labels, stats, centroids);

        // Contar componentes que podem ser texto
        int textComponents = 0;
        for (int i = 1; i < connectedComponents; i++) {
            double area = stats.get(i, Imgproc.CC_STAT_AREA)[0];
            if (area > 20 && area < 1000) {
                textComponents++;
            }
        }

        logger.debug("Componentes de texto detectados: {}", textComponents);
        return textComponents > 15;
    }

    private boolean hasRGDimensions(Mat image) {
        double ratio = (double) image.width() / image.height();
        logger.debug("Proporção largura/altura da imagem: {:.2f}", ratio);
        // RG é tipicamente mais largo do que alto, proporção em torno de 1.5
        return ratio > 1.4 && ratio < 1.7;
    }

    private int countRGKeywords(String text) {
        if (text == null || text.isEmpty()) return 0;

        String textNormalized = text.toLowerCase()
                .replaceAll("[áàãâä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòõôö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c");

        int score = 0;

        if (Pattern.compile("\\br[\\s.-]*g\\b").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("registro\\s+geral|cedula\\s+de\\s+identidade").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("secretaria|seguranca|ident[ií]ficacao").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("\\bssp\\b|instit(uto)?|estado").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("brasil|republica|federativa").matcher(textNormalized).find()) score += 1;
        if (Pattern.compile("filia(c|ç)(a|ã)o|filho\\s+de|pai|m(a|ã)e").matcher(textNormalized).find()) score += 3;
        if (Pattern.compile("\\d{1,3}\\.\\d{3}\\.\\d{3}|\\d{6,9}(-[0-9A-Za-z])?").matcher(text).find()) score += 2;
        if (Pattern.compile("nascimento|data|nasc\\.").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("expedi(c|ç)(a|ã)o|emiss(a|ã)o|emitido").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("natural(idade)?|origem|local").matcher(textNormalized).find()) score += 1;
        if (Pattern.compile("assinatura|digital|biometria").matcher(textNormalized).find()) score += 1;

        return score;
    }
    //    -------------------------------------------------------------------------------
    //    FIM FUNÇÕES DE VALIDAÇÃO DO RG


    // -----------------------------------------------------------------------------------------------------------------


    //    FUNÇÕES DE VALIDAÇÃO DO CPF
    //    -------------------------------------------------------------------------------
    private String validateCPF(File frontFile, File a) {
        logger.info("Validando se a imagem é de um CPF brasileiro");

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            logger.info("Diretório de uploads criado: {}", UPLOAD_DIR);
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String frontSavedPath = UPLOAD_DIR + "front_" + timestamp + ".jpg";

        Mat frontImage = Imgcodecs.imread(frontFile.getAbsolutePath());
        if (frontImage.empty()) {
            logger.error("Erro ao carregar imagem da frente");
            return "Erro - Imagem não carregada";
        }

        logImageColorInfo(frontImage);

        Imgcodecs.imwrite(frontSavedPath, frontImage);
        logger.info("Imagem salva - Frente: '{}'", frontSavedPath);

        Mat frontResized = new Mat();
        Imgproc.resize(frontImage, frontResized, new Size(frontImage.cols() * 2, frontImage.rows() * 2), 0, 0, Imgproc.INTER_CUBIC);

        Mat frontGray = new Mat();
        Imgproc.cvtColor(frontResized, frontGray, Imgproc.COLOR_BGR2GRAY);
        Mat frontEnhanced = new Mat();
        Imgproc.equalizeHist(frontGray, frontEnhanced);
        String frontEnhancedSavedPath = UPLOAD_DIR + "front_enhanced_" + timestamp + ".jpg";
        Imgcodecs.imwrite(frontEnhancedSavedPath, frontEnhanced);

        // Inicializar Tesseract
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(LIBS_PATH);
        tesseract.setLanguage("por");
        tesseract.setPageSegMode(6);

        String extractedTextOriginal;
        try {
            extractedTextOriginal = tesseract.doOCR(new File(frontSavedPath));
            logger.debug("Texto extraído da imagem original: {}", extractedTextOriginal);
        } catch (TesseractException e) {
            logger.error("Erro ao realizar OCR na imagem original: {}", e.getMessage());
            extractedTextOriginal = "";
        }

        String extractedTextEnhanced;
        try {
            extractedTextEnhanced = tesseract.doOCR(new File(frontEnhancedSavedPath));
            logger.debug("Texto extraído da imagem com contraste melhorado: {}", extractedTextEnhanced);
        } catch (TesseractException e) {
            logger.error("Erro ao realizar OCR na imagem com contraste melhorado: {}", e.getMessage());
            extractedTextEnhanced = "";
        }

        String extractedText = extractedTextOriginal;
        int originalScore = countCPFKeywords(extractedTextOriginal);
        int enhancedScore = countCPFKeywords(extractedTextEnhanced);
        if (enhancedScore > originalScore) {
            extractedText = extractedTextEnhanced;
            logger.debug("Texto da imagem com contraste melhorado escolhido por conter mais palavras-chave (score: {} vs {})", enhancedScore, originalScore);
        } else {
            logger.debug("Texto da imagem original escolhido por conter mais palavras-chave (score: {} vs {})", originalScore, enhancedScore);
        }

        String nonCPFResult = isNotCPF(extractedText);
        if (!nonCPFResult.equals("CPF")) {
            logger.info("Documento identificado como {}, não como CPF", nonCPFResult);
            return "Inválido - Documento é " + nonCPFResult + ", não um CPF brasileiro";
        }

        boolean hasCorrectColor = hasCPFColorPattern(frontImage);
        double colorSimilarity = calculateColorSimilarity(frontImage);
        boolean hasContrastText = hasHighContrastText(frontImage);
        boolean hasCPFDims = hasCPFDimensions(frontImage);

        int score = 0;

        if (hasCorrectColor) {
            score += 6;
            logger.debug("Documento possui cor azul característica do CPF: +6 pontos");
        }
        if (colorSimilarity > 0.2) {
            int colorPoints = Math.min(3, (int)(colorSimilarity * 10));
            score += colorPoints;
            logger.debug("Similaridade de cor com o azul padrão do CPF (#36b3f3): {}%, +{} pontos",
                    Math.round(colorSimilarity * 100), colorPoints);
        }
        if (hasContrastText) {
            score += 1;
            logger.debug("Documento possui contraste típico de texto: +1 ponto");
        }
        if (hasCPFDims) {
            score += 1;
            logger.debug("Documento possui dimensões retangulares comuns: +1 ponto");
        }

        boolean hasCPFText = Pattern.compile("\\bc[\\s.-]*p[\\s.-]*f\\b", Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasCPFText) {
            if (isCPFCentralText(extractedText)) {
                score += 3;
                logger.debug("'CPF' como texto central encontrado (típico do documento CPF): +3 pontos");
            } else {
                score += 1;
                logger.debug("Texto 'CPF' encontrado, mas apenas como campo/atributo: +1 ponto");
            }
        }

        boolean hasMinisterioFazenda = Pattern.compile("minist[eé]rio\\s+da\\s+fazenda|receita\\s+federal",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasMinisterioFazenda) {
            score += 4;
            logger.debug("Referência a 'Ministério da Fazenda/Receita Federal' encontrada: +4 pontos");
        }

        boolean hasCpfNumber = Pattern.compile("\\d{3}[.\\s]?\\d{3}[.\\s]?\\d{3}[-—\\s]?\\d{2}").matcher(extractedText).find();
        if (hasCpfNumber) {
            if (isCPFNumberProminent(extractedText)) {
                score += 3;
                logger.debug("Formato numérico de CPF em destaque encontrado: +3 pontos");
            } else {
                score += 1;
                logger.debug("Formato numérico de CPF encontrado, mas apenas como campo: +1 ponto");
            }
        }

        boolean hasRepublicaFederativa = Pattern.compile("rep[uú]blica\\s+federativa\\s+do\\s+brasil",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasRepublicaFederativa) {
            score += 2;
            logger.debug("Texto 'República Federativa do Brasil' encontrado: +2 pontos");
        }

        boolean hasCadastroText = Pattern.compile("cadastro\\s+de\\s+pessoas?\\s+f[ií]sicas?",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasCadastroText) {
            score += 4;
            logger.debug("Texto 'Cadastro de Pessoa Física' por extenso encontrado: +4 pontos");
        }

        logger.info("Pontuação total da verificação: " + score);

        if (score >= 10) {
            logger.info("Documento validado como Válido (Alta confiança) - Pontuação: {}", score);
            return "Válido - Alta confiança";
        } else if (score >= 7) {
            logger.info("Documento validado como Válido (Média confiança) - Pontuação: {}", score);
            return "Válido - Média confiança";
        } else {
            logger.info("Documento validado como Inválido - Não aparenta ser um CPF - Pontuação: {}", score);
            return "Inválido - Não aparenta ser um CPF brasileiro";
        }
    }

    private void logImageColorInfo(Mat image) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar meanColor = Core.mean(image);

        double meanBlue = meanColor.val[0];
        double meanGreen = meanColor.val[1];
        double meanRed = meanColor.val[2];

        Scalar meanHSV = Core.mean(hsv);
        double meanHue = meanHSV.val[0];
        double meanSaturation = meanHSV.val[1];
        double meanValue = meanHSV.val[2];

        double blueRatio = meanBlue / (meanRed + meanGreen + meanBlue);

        String hexColor = String.format("#%02X%02X%02X",
                (int)meanRed,
                (int)meanGreen,
                (int)meanBlue);

        logger.info("Análise de cor da imagem:");
        logger.info("Cor média (BGR): B={}, G={}, R={}", meanBlue, meanGreen, meanRed);
        logger.info("Cor média (Hex): {}", hexColor);
        logger.info("Valores HSV: H={} ({}°), S={}%, V={}%",
                meanHue,
                Math.round(meanHue * 2),
                Math.round(meanSaturation / 255 * 100),
                Math.round(meanValue / 255 * 100));
        logger.info("Predominância de azul: {}%", Math.round(blueRatio * 100));

        double cpfBlueProximity = calculateCPFBlueProximity(meanRed, meanGreen, meanBlue);
        logger.info("Proximidade com azul padrão do CPF (#36b3f3): {}%",
                Math.round(cpfBlueProximity * 100));
    }

    private double calculateCPFBlueProximity(double r, double g, double b) {
        double cpfR = 52;
        double cpfG = 107;
        double cpfB = 138;
        double distance = Math.sqrt(
                Math.pow((r - cpfR) / 255, 2) +
                        Math.pow((g - cpfG) / 255, 2) +
                        Math.pow((b - cpfB) / 255, 2)
        );
        return Math.max(0, 1 - (distance / Math.sqrt(3)));
    }

    private String isNotCPF(String text) {
        if (text == null || text.isEmpty()) {
            return "CPF";
        }

        String textLower = text.toLowerCase();

        Map<String, List<String>> documentPatterns = new HashMap<>();

        documentPatterns.put("CNH", Arrays.asList(
                "carteira\\s+nacional\\s+de\\s+habilita[cç][aã]o",
                "permiss[aã]o\\s+para\\s+dirigir",
                "categoria",
                "acc",
                "1[aª]\\s+habilita[cç][aã]o",
                "renach",
                "condutor",
                "detran",
                "n[aã]o\\s+hab\\s+cat"
        ));

        documentPatterns.put("RG", Arrays.asList(
                "registro\\s+geral",
                "c[eé]dula\\s+de\\s+identidade",
                "secretaria\\s+de\\s+seguran[cç]a",
                "ssp",
                "instituto\\s+de\\s+identifica[cç][aã]o",
                "\\brg[\\s:]*\\d+"
        ));

        documentPatterns.put("CTPS", Arrays.asList(
                "carteira\\s+de\\s+trabalho",
                "previdência\\s+social",
                "\\bctps\\b",
                "ministério\\s+do\\s+trabalho",
                "contrato\\s+de\\s+trabalho",
                "série\\s+\\d+",
                "\\bfgts\\b"
        ));

        documentPatterns.put("Título de Eleitor", Arrays.asList(
                "t[ií]tulo\\s+de\\s+eleitor",
                "tribunal\\s+superior\\s+eleitoral",
                "justi[çc]a\\s+eleitoral",
                "zona\\s+eleitoral",
                "se[çc][aã]o\\s+eleitoral"
        ));

        Map<String, Integer> matchCounts = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : documentPatterns.entrySet()) {
            String docType = entry.getKey();
            List<String> patterns = entry.getValue();

            int matchCount = 0;
            List<String> matchedPatterns = new ArrayList<>();

            for (String pattern : patterns) {
                if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(textLower).find()) {
                    matchCount++;
                    matchedPatterns.add(pattern);
                }
            }

            if (matchCount > 0) {
                logger.debug("Encontrados {} padrões de {}: {}", matchCount, docType, matchedPatterns);
            }

            matchCounts.put(docType, matchCount);
        }

        Map<String, Integer> thresholds = new HashMap<>();
        thresholds.put("CNH", 2);
        thresholds.put("RG", 2);
        thresholds.put("CTPS", 2);
        thresholds.put("Título de Eleitor", 2);

        String detectedType = "CPF";
        int maxMatches = 0;

        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String docType = entry.getKey();
            int count = entry.getValue();
            int threshold = thresholds.getOrDefault(docType, 2);

            if (count >= threshold && count > maxMatches) {
                maxMatches = count;
                detectedType = docType;
            }
        }

        return detectedType;
    }

    private boolean isCPFCentralText(String text) {
        Pattern centralCPFPattern = Pattern.compile("^\\s*cpf\\s*$|^\\s*c\\.?p\\.?f\\.?\\s*$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        return centralCPFPattern.matcher(text).find();
    }

    private boolean isCPFNumberProminent(String text) {
        Pattern prominentCPFPattern = Pattern.compile("^\\s*\\d{3}[.\\s]?\\d{3}[.\\s]?\\d{3}[-—\\s]?\\d{2}\\s*$",
                Pattern.MULTILINE);
        return prominentCPFPattern.matcher(text).find();
    }

    private boolean hasCPFColorPattern(Mat image) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);
        Scalar lowerBlue = new Scalar(89, 108, 88);  // H-10, S-50, V-50
        Scalar upperBlue = new Scalar(109, 208, 188); // H+10, S+50, V+50
        Mat blueMask = new Mat();
        Core.inRange(hsv, lowerBlue, upperBlue, blueMask);
        double bluePixels = Core.countNonZero(blueMask);
        double totalPixels = image.rows() * image.cols();
        double blueRatio = bluePixels / totalPixels;
        logger.debug("Proporção de pixels azuis na imagem: {}%", Math.round(blueRatio * 100));
        return blueRatio > 0.25;
    }

    private double calculateColorSimilarity(Mat image) {
        Mat resized = new Mat();
        if (image.width() > 800 || image.height() > 800) {
            Size newSize = new Size(image.width() / 2, image.height() / 2);
            Imgproc.resize(image, resized, newSize);
        } else {
            resized = image.clone();
        }
        Mat hsv = new Mat();
        Imgproc.cvtColor(resized, hsv, Imgproc.COLOR_BGR2HSV);
        Scalar cpfBlueHSV = new Scalar(99, 158, 138);
        Scalar lowerBound = new Scalar(cpfBlueHSV.val[0] - 10, cpfBlueHSV.val[1] - 50, cpfBlueHSV.val[2] - 50);
        Scalar upperBound = new Scalar(cpfBlueHSV.val[0] + 10, cpfBlueHSV.val[1] + 50, cpfBlueHSV.val[2] + 50);
        Mat mask = new Mat();
        Core.inRange(hsv, lowerBound, upperBound, mask);
        double similarPixels = Core.countNonZero(mask);
        double totalPixels = mask.total();
        double similarity = similarPixels / totalPixels;
        logger.debug("Similaridade de cor com o azul padrão do CPF (#346B8A): {}%", Math.round(similarity * 100));
        return similarity;
    }

    private boolean hasHighContrastText(Mat image) {
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(gray, thresh, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV, 11, 2);

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        int connectedComponents = Imgproc.connectedComponentsWithStats(
                thresh, labels, stats, centroids);

        int textComponents = 0;
        for (int i = 1; i < connectedComponents; i++) {
            double area = stats.get(i, Imgproc.CC_STAT_AREA)[0];
            if (area > 20 && area < 1000) {
                textComponents++;
            }
        }

        logger.debug("Componentes de texto detectados: {}", textComponents);
        return textComponents > 15;
    }

    private boolean hasCPFDimensions(Mat image) {
        double ratio = (double) image.width() / image.height();
        logger.debug("Proporção largura/altura da imagem: {:.2f}", ratio);
        return ratio > 1.3 && ratio < 1.7;
    }

    private int countCPFKeywords(String text) {
        if (text == null || text.isEmpty()) return 0;

        String textNormalized = text.toLowerCase()
                .replaceAll("[áàãâä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòõôö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c");

        int score = 0;

        if (Pattern.compile("\\bc[\\s.-]*p[\\s.-]*f\\b").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("cadastro|pessoa|fisica").matcher(textNormalized).find()) score += 1;
        if (Pattern.compile("ministerio|fazenda|receita|federal").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("\\d{3}[.\\s]?\\d{3}[.\\s]?\\d{3}[-—\\s]?\\d{2}").matcher(text).find()) score += 3;
        if (Pattern.compile("brasil|republica|federativa").matcher(textNormalized).find()) score += 1;
        if (Pattern.compile("documento|identidade|identificacao").matcher(textNormalized).find()) score += 1;

        return score;
    }
    //    -------------------------------------------------------------------------------
    //    FIM FUNÇÕES DE VALIDAÇÃO DO CPF


// -----------------------------------------------------------------------------------------------------------------


    //    FUNÇÕES DE VALIDAÇÃO DA CNH
    //    -------------------------------------------------------------------------------
    private String validateCNH(File frontFile, File backFile) {
        logger.info("Validando se a imagem é de uma CNH brasileira");

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            logger.info("Diretório de uploads criado: {}", UPLOAD_DIR);
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String frontSavedPath = UPLOAD_DIR + "front_" + timestamp + ".jpg";
        String backSavedPath = null;

        if (backFile != null) {
            backSavedPath = UPLOAD_DIR + "back_" + timestamp + ".jpg";
        }

        Mat frontImage = Imgcodecs.imread(frontFile.getAbsolutePath());
        if (frontImage.empty()) {
            logger.error("Erro ao carregar imagem da frente");
            return "Erro - Imagem não carregada";
        }

        logImageColorInfoCNH(frontImage);

        Imgcodecs.imwrite(frontSavedPath, frontImage);
        logger.info("Imagem salva - Frente: '{}'", frontSavedPath);

        Mat backImage = null;
        if (backFile != null) {
            backImage = Imgcodecs.imread(backFile.getAbsolutePath());
            if (!backImage.empty()) {
                Imgcodecs.imwrite(backSavedPath, backImage);
                logger.info("Imagem salva - Verso: '{}'", backSavedPath);
            } else {
                logger.warn("Erro ao carregar imagem do verso");
            }
        }

        Mat frontResized = new Mat();
        Imgproc.resize(frontImage, frontResized, new Size(frontImage.cols() * 2, frontImage.rows() * 2), 0, 0, Imgproc.INTER_CUBIC);

        Mat frontGray = new Mat();
        Imgproc.cvtColor(frontResized, frontGray, Imgproc.COLOR_BGR2GRAY);
        Mat frontEnhanced = new Mat();
        Imgproc.equalizeHist(frontGray, frontEnhanced);
        String frontEnhancedSavedPath = UPLOAD_DIR + "front_enhanced_" + timestamp + ".jpg";
        Imgcodecs.imwrite(frontEnhancedSavedPath, frontEnhanced);

        // Inicializar Tesseract
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(LIBS_PATH);
        tesseract.setLanguage("por");
        tesseract.setPageSegMode(6);

        String extractedTextOriginal;
        try {
            extractedTextOriginal = tesseract.doOCR(new File(frontSavedPath));
            logger.debug("Texto extraído da imagem original: {}", extractedTextOriginal);
        } catch (TesseractException e) {
            logger.error("Erro ao realizar OCR na imagem original: {}", e.getMessage());
            extractedTextOriginal = "";
        }

        String extractedTextEnhanced;
        try {
            extractedTextEnhanced = tesseract.doOCR(new File(frontEnhancedSavedPath));
            logger.debug("Texto extraído da imagem com contraste melhorado: {}", extractedTextEnhanced);
        } catch (TesseractException e) {
            logger.error("Erro ao realizar OCR na imagem com contraste melhorado: {}", e.getMessage());
            extractedTextEnhanced = "";
        }

        String extractedText = extractedTextOriginal;
        int originalScore = countCNHKeywords(extractedTextOriginal);
        int enhancedScore = countCNHKeywords(extractedTextEnhanced);
        if (enhancedScore > originalScore) {
            extractedText = extractedTextEnhanced;
            logger.debug("Texto da imagem com contraste melhorado escolhido por conter mais palavras-chave (score: {} vs {})", enhancedScore, originalScore);
        } else {
            logger.debug("Texto da imagem original escolhido por conter mais palavras-chave (score: {} vs {})", originalScore, enhancedScore);
        }

        String nonCNHResult = isNotCNH(extractedText);
        if (!nonCNHResult.equals("CNH")) {
            logger.info("Documento identificado como {}, não como CNH", nonCNHResult);
            return "Inválido - Documento é " + nonCNHResult + ", não uma CNH brasileira";
        }

        boolean hasCorrectColor = hasCNHColorPattern(frontImage);
        double colorSimilarity = calculateCNHColorSimilarity(frontImage);
        boolean hasContrastText = hasHighContrastTextCNH(frontImage);
        boolean hasCNHDims = hasCNHDimensions(frontImage);

        int score = 0;

        if (hasCorrectColor) {
            score += 6;
            logger.debug("Documento possui cor cinza-bege característica da CNH: +6 pontos");
        }
        if (colorSimilarity > 0.2) {
            int colorPoints;
            if (colorSimilarity >= 0.8) {
                colorPoints = 6;
            } else if (colorSimilarity >= 0.5) {
                colorPoints = 4;
            } else {
                colorPoints = 2;
            }
            score += colorPoints;
            logger.debug("Similaridade de cor com o padrão da CNH (#949488 - #A29A8F): {}%, +{} pontos",
                    Math.round(colorSimilarity * 100), colorPoints);
        }
        if (hasContrastText) {
            score += 1;
            logger.debug("Documento possui contraste típico de texto: +1 ponto");
        }
        if (hasCNHDims) {
            score += 1;
            logger.debug("Documento possui dimensões retangulares comuns: +1 ponto");
        }

        boolean hasCNHText = Pattern.compile("\\bc[\\s.-]*n[\\s.-]*h\\b|carteira[\\s\\w]*naciona[li1]+|habilita[cç][aã][oõ0]?",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasCNHText) {
            if (isCNHCentralText(extractedText)) {
                score += 7;
                logger.debug("'CNH' ou 'Carteira Nacional de Habilitação' como texto central encontrado: +7 pontos");
            } else {
                score += 5;
                logger.debug("Texto 'CNH' ou 'Carteira Nacional de Habilitação' encontrado: +5 pontos");
            }
        }

        boolean hasDetran = Pattern.compile("detran|denatran|departamento[\\s\\w]*naciona[li1z]+[\\s\\w]*tr[aâãn]s[i1][tn]o?",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasDetran) {
            score += 6;
            logger.debug("Referência a 'DETRAN/DENATRAN' encontrada: +6 pontos");
        }

        boolean hasCnhNumber = Pattern.compile("\\d{9,11}|registro\\s+n[°º]\\s*\\d+").matcher(extractedText).find();
        if (hasCnhNumber) {
            score += 3;
            logger.debug("Formato numérico de registro da CNH encontrado: +3 pontos");
        }

        boolean hasRepublicaFederativa = Pattern.compile("rep[uú]blica\\s+federativa\\s+do\\s+brasil",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasRepublicaFederativa) {
            score += 2;
            logger.debug("Texto 'República Federativa do Brasil' encontrado: +2 pontos");
        }

        boolean hasCategoria = Pattern.compile("categoria|cat\\.?|ACC|[AB][12]",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasCategoria) {
            score += 6;
            logger.debug("Texto de categoria ou habilitação encontrado: +6 pontos");
        }

        boolean hasValidade = Pattern.compile("val(i|1)dade|valida\\s+at[ée]|data\\s+val(i|1)dade",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasValidade) {
            score += 2;
            logger.debug("Texto de validade encontrado: +2 pontos");
        }

        boolean hasPrimeiraHabilitacao = Pattern.compile("1[aªº]\\s+habilita[çc][aã]o|primeira\\s+habilita[çc][aã]o",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasPrimeiraHabilitacao) {
            score += 3;
            logger.debug("Texto de primeira habilitação encontrado: +3 pontos");
        }

        boolean hasObservacoes = Pattern.compile("observa[çc][oõ]es|observ\\.:",
                Pattern.CASE_INSENSITIVE).matcher(extractedText).find();
        if (hasObservacoes) {
            score += 1;
            logger.debug("Campo de observações encontrado: +1 ponto");
        }

        logger.info("Pontuação total da verificação: {}", score);

        if (score >= 15) {
            logger.info("Documento validado como Válido (Alta confiança) - Pontuação: {}", score);
            return "Válido - Alta confiança";
        } else if (score >= 10) {
            logger.info("Documento validado como Válido (Média confiança) - Pontuação: {}", score);
            return "Válido - Média confiança";
        } else {
            logger.info("Documento validado como Inválido - Não aparenta ser uma CNH - Pontuação: {}", score);
            return "Inválido - Não aparenta ser uma CNH brasileira";
        }
    }

    private void logImageColorInfoCNH(Mat image) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar meanColor = Core.mean(image);

        double meanBlue = meanColor.val[0];
        double meanGreen = meanColor.val[1];
        double meanRed = meanColor.val[2];

        Scalar meanHSV = Core.mean(hsv);
        double meanHue = meanHSV.val[0];
        double meanSaturation = meanHSV.val[1];
        double meanValue = meanHSV.val[2];

        double maxChannel = Math.max(Math.max(meanRed, meanGreen), meanBlue);
        double minChannel = Math.min(Math.min(meanRed, meanGreen), meanBlue);
        double colorDeviation = (maxChannel - minChannel) / 255.0;
        double grayRatio = 1.0 - colorDeviation;

        String hexColor = String.format("#%02X%02X%02X",
                (int)meanRed,
                (int)meanGreen,
                (int)meanBlue);

        logger.info("Análise de cor da imagem:");
        logger.info("Cor média (BGR): B={}, G={}, R={}", meanBlue, meanGreen, meanRed);
        logger.info("Cor média (Hex): {}", hexColor);
        logger.info("Valores HSV: H={} ({}°), S={}%, V={}%",
                meanHue,
                Math.round(meanHue * 2),
                Math.round(meanSaturation / 255 * 100),
                Math.round(meanValue / 255 * 100));
        logger.info("Predominância de cinza: {}%", Math.round(grayRatio * 100));

        double cnhColorProximity = calculateCNHColorProximity(meanRed, meanGreen, meanBlue);
        logger.info("Proximidade com cor padrão da CNH (#949488 - #A29A8F): {}%",
                Math.round(cnhColorProximity * 100));
    }

    private double calculateCNHColorProximity(double r, double g, double b) {
        double cnhR1 = 148;
        double cnhG1 = 148;
        double cnhB1 = 136;

        double cnhR2 = 162;
        double cnhG2 = 154;
        double cnhB2 = 143;

        double distance1 = Math.sqrt(
                Math.pow((r - cnhR1) / 255, 2) +
                        Math.pow((g - cnhG1) / 255, 2) +
                        Math.pow((b - cnhB1) / 255, 2)
        );

        double distance2 = Math.sqrt(
                Math.pow((r - cnhR2) / 255, 2) +
                        Math.pow((g - cnhG2) / 255, 2) +
                        Math.pow((b - cnhB2) / 255, 2)
        );

        double minDistance = Math.min(distance1, distance2);
        return Math.max(0, 1 - (minDistance / Math.sqrt(3)));
    }

    private String isNotCNH(String text) {
        if (text == null || text.isEmpty()) {
            return "CNH";
        }

        String textLower = text.toLowerCase();

        Map<String, List<String>> documentPatterns = new HashMap<>();

        documentPatterns.put("CPF", Arrays.asList(
                "cadastro\\s+de\\s+pessoa\\s+f[ií]sica",
                "cpf",
                "minist[eé]rio\\s+da\\s+fazenda",
                "receita\\s+federal"
        ));

        documentPatterns.put("RG", Arrays.asList(
                "registro\\s+geral",
                "c[eé]dula\\s+de\\s+identidade",
                "secretaria\\s+de\\s+seguran[cç]a",
                "ssp",
                "instituto\\s+de\\s+identifica[cç][aã]o",
                "\\brg[\\s:]*\\d+"
        ));

        documentPatterns.put("CTPS", Arrays.asList(
                "carteira\\s+de\\s+trabalho",
                "previdência\\s+social",
                "\\bctps\\b",
                "ministério\\s+do\\s+trabalho",
                "contrato\\s+de\\s+trabalho",
                "série\\s+\\d+",
                "\\bfgts\\b"
        ));

        documentPatterns.put("Título de Eleitor", Arrays.asList(
                "t[ií]tulo\\s+de\\s+eleitor",
                "tribunal\\s+superior\\s+eleitoral",
                "justi[çc]a\\s+eleitoral",
                "zona\\s+eleitoral",
                "se[çc][aã]o\\s+eleitoral"
        ));

        Map<String, Integer> matchCounts = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : documentPatterns.entrySet()) {
            String docType = entry.getKey();
            List<String> patterns = entry.getValue();

            int matchCount = 0;
            List<String> matchedPatterns = new ArrayList<>();

            for (String pattern : patterns) {
                if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(textLower).find()) {
                    matchCount++;
                    matchedPatterns.add(pattern);
                }
            }

            if (matchCount > 0) {
                logger.debug("Encontrados {} padrões de {}: {}", matchCount, docType, matchedPatterns);
            }

            matchCounts.put(docType, matchCount);
        }

        Map<String, Integer> thresholds = new HashMap<>();
        thresholds.put("CPF", 2);
        thresholds.put("RG", 2);
        thresholds.put("CTPS", 2);
        thresholds.put("Título de Eleitor", 2);

        String detectedType = "CNH";
        int maxMatches = 0;

        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String docType = entry.getKey();
            int count = entry.getValue();
            int threshold = thresholds.getOrDefault(docType, 2);

            if (count >= threshold && count > maxMatches) {
                maxMatches = count;
                detectedType = docType;
            }
        }

        return detectedType;
    }

    private boolean isCNHCentralText(String text) {
        Pattern centralCNHPattern = Pattern.compile("^\\s*cnh\\s*$|^\\s*c\\.?n\\.?h\\.?\\s*$|^\\s*carteira\\s+nacional\\s+de\\s+habilita[çc][ãa]o\\s*$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        return centralCNHPattern.matcher(text).find();
    }

    private boolean isCNHNumberProminent(String text) {
        Pattern prominentCNHPattern = Pattern.compile("^\\s*\\d{9,11}\\s*$|^\\s*registro\\s+n[°º]\\s*\\d+\\s*$",
                Pattern.MULTILINE);
        return prominentCNHPattern.matcher(text).find();
    }

    private boolean hasCNHColorPattern(Mat image) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

        // Define duas faixas de cores para verificar
        // #949488 = RGB(148, 148, 136) em HSV ~ (60, 8, 148)
        // #A29A8F = RGB(162, 154, 143) em HSV ~ (34, 12, 162)

        // Primeira faixa (cinza esverdeado)
        Scalar lowerGray1 = new Scalar(50, 0, 128);  // H-10, S com tolerância, V-20
        Scalar upperGray1 = new Scalar(70, 20, 168); // H+10, S com tolerância, V+20

        // Segunda faixa (bege)
        Scalar lowerGray2 = new Scalar(24, 0, 143);  // H-10, S com tolerância, V-20
        Scalar upperGray2 = new Scalar(44, 30, 183); // H+10, S com tolerância, V+20

        Mat grayMask1 = new Mat();
        Mat grayMask2 = new Mat();
        Core.inRange(hsv, lowerGray1, upperGray1, grayMask1);
        Core.inRange(hsv, lowerGray2, upperGray2, grayMask2);

        Mat combinedMask = new Mat();
        Core.bitwise_or(grayMask1, grayMask2, combinedMask);

        double grayPixels = Core.countNonZero(combinedMask);
        double totalPixels = image.rows() * image.cols();
        double grayRatio = grayPixels / totalPixels;

        logger.debug("Proporção de pixels na cor da CNH na imagem: {}%", Math.round(grayRatio * 100));
        return grayRatio > 0.25;
    }

    private double calculateCNHColorSimilarity(Mat image) {
        Scalar meanColor = Core.mean(image);
        double meanRed = meanColor.val[2];
        double meanGreen = meanColor.val[1];
        double meanBlue = meanColor.val[0];

        double cnhR = (148 + 162) / 2.0;
        double cnhG = (148 + 154) / 2.0;
        double cnhB = (136 + 143) / 2.0;

        double distance = Math.sqrt(
                Math.pow((meanRed - cnhR) / 255, 2) +
                        Math.pow((meanGreen - cnhG) / 255, 2) +
                        Math.pow((meanBlue - cnhB) / 255, 2)
        );

        double similarity = Math.max(0, 1 - (distance / Math.sqrt(3)));
        logger.debug("Similaridade de cor com as cores padrão da CNH: {}%", Math.round(similarity * 100));
        return similarity;
    }

    private boolean hasHighContrastTextCNH(Mat image) {
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(gray, thresh, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV, 11, 2);

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        int connectedComponents = Imgproc.connectedComponentsWithStats(
                thresh, labels, stats, centroids);

        int textComponents = 0;
        for (int i = 1; i < connectedComponents; i++) {
            double area = stats.get(i, Imgproc.CC_STAT_AREA)[0];
            if (area > 20 && area < 1000) {
                textComponents++;
            }
        }

        logger.debug("Componentes de texto detectados: {}", textComponents);
        return textComponents > 15;
    }

    private boolean hasCNHDimensions(Mat image) {
        double ratio = (double) image.width() / image.height();
        logger.debug("Proporção largura/altura da imagem: {:.2f}", ratio);
        return ratio > 1.4 && ratio < 1.6;
    }

    private int countCNHKeywords(String text) {
        if (text == null || text.isEmpty()) return 0;

        String textNormalized = text.toLowerCase()
                .replaceAll("[áàãâä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòõôö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c");

        int score = 0;

        if (Pattern.compile("\\bc[\\s.-]*n[\\s.-]*h\\b").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("cart(e|ei)ra|nacional|habilita(c|ç)(a|ã)o").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("detran|denatran|tr(a|â)nsito").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("categoria|permiss(a|ã)o|acc|[ab][12]").matcher(textNormalized).find()) score += 3;
        if (Pattern.compile("brasil|republica|federativa").matcher(textNormalized).find()) score += 1;
        if (Pattern.compile("condutor|motorista|dirigir").matcher(textNormalized).find()) score += 2;
        if (Pattern.compile("\\d{9,11}|registro").matcher(text).find()) score += 2;
        if (Pattern.compile("val(i|1)dade|data|renova(c|ç)(a|ã)o").matcher(textNormalized).find()) score += 1;
        if (Pattern.compile("1(a|ª)\\s+habilita(c|ç)(a|ã)o|primeira").matcher(textNormalized).find()) score += 3;
        if (Pattern.compile("observa(c|ç)(o|õ)es|restricoes|exercer\\s+atividade\\s+remunerada").matcher(textNormalized).find()) score += 2;

        return score;
    }
    //    -------------------------------------------------------------------------------
    //    FIM FUNÇÕES DE VALIDAÇÃO DA CNH

// -----------------------------------------------------------------------------------------------------------------

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }

    private Metadata createMetadata(MultipartFile file, Double latitude, Double longitude, String ipOrigem) {
        String fileName = file.getOriginalFilename();
        String fileId = java.util.UUID.randomUUID().toString();
        return new Metadata(latitude, longitude, ipOrigem, fileName, fileId);
    }
}