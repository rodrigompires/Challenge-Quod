package br.com.fiap.challenge_quod.challenge_quod.util;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.io.File;


public class FacialBiometricComparator {

    private static final Logger logger = LoggerFactory.getLogger(FacialBiometricComparator.class);

    private static final double COORDINATE_THRESHOLD = 0.001;
    private static final double SIMILARITY_THRESHOLD = 0.65;
    private static final double FRAUD_THRESHOLD = 0.4;
    private static final double SSIM_THRESHOLD = 0.04;
    private static final double CONFIDENCE_THRESHOLD = 0.6;
    private static final boolean FORCE_FRAUD_TEST = true; // Para simular fraude nas coordenadas

    private static final String LIBS_PATH = System.getProperty("user.dir") + File.separator + "libs" + File.separator;
    private static final String FACENET_MODEL_PATH = LIBS_PATH + "arcfaceresnet100-8.onnx";
    private static final String FACE_DETECTION_MODEL_PATH = LIBS_PATH + "deploy.prototxt";
    private static final String FACE_DETECTION_WEIGHTS_PATH = LIBS_PATH + "res10_300x300_ssd_iter_140000.caffemodel";

    private static Net faceDetector;
    private static Net faceNet;

    static {
        try {
            logger.info("Tentando carregar a biblioteca nativa do OpenCV...");
            String libPath = LIBS_PATH + "opencv_java411.dll";
            File dllFile = new File(libPath);
            if (!dllFile.exists()) {
                throw new RuntimeException("Arquivo opencv_java411.dll não encontrado em: " + libPath);
            }
            System.load(libPath);
            logger.info("Biblioteca nativa do OpenCV carregada com sucesso. Versão: {}", Core.getVersionString());

            File protoFile = new File(FACE_DETECTION_MODEL_PATH);
            File weightsFile = new File(FACE_DETECTION_WEIGHTS_PATH);
            if (!protoFile.exists() || !weightsFile.exists()) {
                throw new RuntimeException("Arquivos de modelo SSD não encontrados: " + FACE_DETECTION_MODEL_PATH + " ou " + FACE_DETECTION_WEIGHTS_PATH);
            }
            faceDetector = Dnn.readNetFromCaffe(FACE_DETECTION_MODEL_PATH, FACE_DETECTION_WEIGHTS_PATH);
            if (faceDetector.empty()) {
                throw new RuntimeException("Erro ao carregar modelo de detecção facial DNN");
            }
            logger.info("Modelo de detecção facial DNN carregado com sucesso.");

            File arcFaceFile = new File(FACENET_MODEL_PATH);
            if (!arcFaceFile.exists()) {
                throw new RuntimeException("Arquivo ArcFace não encontrado em: " + FACENET_MODEL_PATH);
            }
            faceNet = Dnn.readNetFromONNX(FACENET_MODEL_PATH);
            if (faceNet.empty()) {
                throw new RuntimeException("Erro ao carregar modelo ArcFace: " + FACENET_MODEL_PATH);
            }
            logger.info("Modelo ArcFace carregado com sucesso de: {}", FACENET_MODEL_PATH);
        } catch (Exception e) {
            logger.error("Erro ao inicializar OpenCV ou modelos: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao carregar OpenCV ou modelos.", e);
        }
    }

    public static class ImageComparisonResult {
        private final boolean facesEqual;
        private final boolean coordinatesEqual;
        private final double similarityScore;
        private final String detailedReason;
        private final double lat1, lon1, lat2, lon2;
        private final double euclideanDistance;
        private final String fraudType;

        public ImageComparisonResult(boolean facesEqual, boolean coordinatesEqual,
                                     double similarityScore, String detailedReason,
                                     double lat1, double lon1, double lat2, double lon2,
                                     double euclideanDistance, String fraudType) {
            this.facesEqual = facesEqual;
            this.coordinatesEqual = coordinatesEqual;
            this.similarityScore = similarityScore;
            this.detailedReason = detailedReason;
            this.lat1 = lat1;
            this.lon1 = lon1;
            this.lat2 = lat2;
            this.lon2 = lon2;
            this.euclideanDistance = euclideanDistance;
            this.fraudType = fraudType;
        }

        public boolean isFacesEqual() { return facesEqual; }
        public boolean isCoordinatesEqual() { return coordinatesEqual; }
        public double getSimilarityScore() { return similarityScore; }
        public String getDetailedReason() { return detailedReason; }
        public double getLat1() { return lat1; }
        public double getLon1() { return lon1; }
        public double getLat2() { return lat2; }
        public double getLon2() { return lon2; }
        public double getEuclideanDistance() { return euclideanDistance; }
        public String getFraudType() { return fraudType; }
    }

    public static ImageComparisonResult compareImages(String neutralImagePath, String smileImagePath,
                                                      double lat1, double lon1, double lat2, double lon2) {
        logger.info("Iniciando comparação de imagens: Neutra={} e Sorriso={}", neutralImagePath, smileImagePath);
        logger.info("Coordenadas originais - Imagem 1 (Neutra): lat={}, lon={} | Imagem 2 (Sorriso): lat={}, lon={}", lat1, lon1, lat2, lon2);

        // Forçar coordenadas diferentes se FORCE_FRAUD_TEST=true
        if (FORCE_FRAUD_TEST) {
            lat2 = (lat2 != 0.0 ? lat2 : 0.0) + 1.0;
            lon2 = (lon2 != 0.0 ? lon2 : 0.0) + 1.0;
            logger.debug("FORCE_FRAUD_TEST ativado: Coordenadas ajustadas - lat2: {}, lon2: {}", lat2, lon2);
        }

        // Validar coordenadas
        if (Double.isNaN(lat1) || Double.isNaN(lon1) || Double.isNaN(lat2) || Double.isNaN(lon2) ||
                Math.abs(lat1) > 90 || Math.abs(lon1) > 180 || Math.abs(lat2) > 90 || Math.abs(lon2) > 180) {
            throw new IllegalArgumentException("Coordenadas inválidas fornecidas.");
        }

        double latDiff = Math.abs(lat1 - lat2);
        double lonDiff = Math.abs(lon1 - lon2);
        boolean areCoordinatesEqual = FORCE_FRAUD_TEST ? false : (latDiff <= COORDINATE_THRESHOLD && lonDiff <= COORDINATE_THRESHOLD);
        logger.info("Diferença de coordenadas - latDiff={}, lonDiff={}, areCoordinatesEqual={}, FORCE_FRAUD_TEST={}",
                latDiff, lonDiff, areCoordinatesEqual, FORCE_FRAUD_TEST);

        Mat neutralImg = Imgcodecs.imread(neutralImagePath);
        Mat smileImg = Imgcodecs.imread(smileImagePath);
        if (neutralImg.empty() || smileImg.empty()) {
            logger.error("Erro ao carregar as imagens: Neutra={} ou Sorriso={}", neutralImagePath, smileImagePath);
            throw new IllegalArgumentException("Não foi possível carregar uma ou ambas as imagens.");
        }
        logger.info("Imagens carregadas com sucesso. Tamanho Neutra: {}x{}, Tamanho Sorriso: {}x{}",
                neutralImg.cols(), neutralImg.rows(), smileImg.cols(), smileImg.rows());

        String logsPath = System.getProperty("user.dir") + File.separator + "logs" + File.separator;
        File logsDir = new File(logsPath);
        if (!logsDir.exists()) logsDir.mkdirs();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String originalNeutralPath = logsPath + "original_neutral_" + timestamp + ".jpg";
        String originalSmilePath = logsPath + "original_smile_" + timestamp + ".jpg";
//        Imgcodecs.imwrite(originalNeutralPath, neutralImg);
//        Imgcodecs.imwrite(originalSmilePath, smileImg);
        logger.debug("Imagens originais salvas em: {} e {}", originalNeutralPath, originalSmilePath);

        FaceDetectionResult neutralDetection = detectAndProcessFace(neutralImg, "neutral_" + timestamp);
        FaceDetectionResult smileDetection = detectAndProcessFace(smileImg, "smile_" + timestamp);

        if (neutralDetection.face == null || smileDetection.face == null) {
            logger.warn("Nenhuma face detectada em uma ou ambas as imagens.");
            neutralImg.release();
            smileImg.release();
            return new ImageComparisonResult(false, areCoordinatesEqual, 0.0,
                    "Face não detectada em uma ou ambas as imagens", lat1, lon1, lat2, lon2, 0.0, "Nenhuma Face Detectada");
        }

        double ssimScore = calculateSSIM(neutralDetection.alignedFace, smileDetection.alignedFace);
        boolean ssimWarning = ssimScore < SSIM_THRESHOLD;
        logger.info("Pontuação SSIM entre as faces: {} (aviso: {})", ssimScore, ssimWarning);

        float[] neutralEmbedding = getFaceEmbedding(neutralDetection.alignedFace);
        float[] smileEmbedding = getFaceEmbedding(smileDetection.alignedFace);
        if (neutralEmbedding == null || smileEmbedding == null) {
            logger.warn("Falha ao gerar embeddings para uma ou ambas as faces.");
            neutralDetection.face.release();
            neutralDetection.alignedFace.release();
            neutralDetection.normalizedFace.release();
            smileDetection.face.release();
            smileDetection.alignedFace.release();
            smileDetection.normalizedFace.release();
            neutralImg.release();
            smileImg.release();
            return new ImageComparisonResult(false, areCoordinatesEqual, 0.0,
                    "Erro ao gerar embeddings faciais", lat1, lon1, lat2, lon2, 0.0, "Erro de Embedding");
        }

        double norm1 = calculateNorm(neutralEmbedding);
        double norm2 = calculateNorm(smileEmbedding);
        logger.debug("Norma do embedding neutra: {}, sorriso: {}", norm1, norm2);
        if (norm1 < 0.1 || norm2 < 0.1) {
            logger.warn("Embedding inválido detectado: norma neutra={}, norma sorriso={}", norm1, norm2);
            neutralDetection.face.release();
            neutralDetection.alignedFace.release();
            neutralDetection.normalizedFace.release();
            smileDetection.face.release();
            smileDetection.alignedFace.release();
            smileDetection.normalizedFace.release();
            neutralImg.release();
            smileImg.release();
            return new ImageComparisonResult(false, areCoordinatesEqual, 0.0,
                    "Embedding inválido (norma muito baixa)", lat1, lon1, lat2, lon2, 0.0, "Embedding Inválido");
        }

        double euclideanDistance = calculateEuclideanDistance(neutralEmbedding, smileEmbedding);
        logger.info("Distância euclidiana entre os embeddings: {}", euclideanDistance);

        double similarityScore = 1.0 - (euclideanDistance / 2.0);
        if (similarityScore < 0) similarityScore = 0.0;
        if (similarityScore > 1) similarityScore = 1.0;
        logger.info("Pontuação de similaridade: {}", similarityScore);

        boolean areFacesEqual;
        String facialReason;
        String fraudType;

        if (similarityScore >= SIMILARITY_THRESHOLD) {
            areFacesEqual = true;
            fraudType = "Nenhuma Fraude Facial";
            facialReason = "Faces consideradas da mesma pessoa (score: " + similarityScore + ", distance: " + euclideanDistance + ")" +
                    (ssimWarning ? " [Aviso: SSIM baixo: " + ssimScore + "]" : "");
        } else if (similarityScore < FRAUD_THRESHOLD) {
            areFacesEqual = false;
            fraudType = "Faces Diferentes";
            facialReason = "Faces consideradas de pessoas diferentes (score: " + similarityScore + ", distance: " + euclideanDistance + ")" +
                    (ssimWarning ? " [Aviso: SSIM baixo: " + ssimScore + "]" : "");
        } else {
            areFacesEqual = false;
            fraudType = "Possível Fraude Facial";
            facialReason = "Possível fraude detectada (score: " + similarityScore + ", distance: " + euclideanDistance + ")" +
                    (ssimWarning ? " [Aviso: SSIM baixo: " + ssimScore + "]" : "");
        }

        String detailedReason = facialReason;
        if (!areCoordinatesEqual) {
            fraudType = "Coordenadas Diferentes" + (areFacesEqual ? "" : " e " + fraudType);
            detailedReason += " [Fraude nas coordenadas: latDiff=" + latDiff + ", lonDiff=" + lonDiff +
                    (FORCE_FRAUD_TEST ? ", coordenadas forçadas para teste de fraude]" : "]");
        } else if (areFacesEqual) {
            fraudType = "Sem Fraude";
            detailedReason = "Nenhuma fraude detectada: faces iguais e coordenadas consistentes.";
        }

        logger.info("Comparação facial: similarityScore={}, euclideanDistance={}, areFacesEqual={}, ssimScore={}, ssimWarning={}",
                similarityScore, euclideanDistance, areFacesEqual, ssimScore, ssimWarning);
        logger.info("Comparação de coordenadas: areCoordinatesEqual={}, latDiff={}, lonDiff={}, FORCE_FRAUD_TEST={}",
                areCoordinatesEqual, latDiff, lonDiff, FORCE_FRAUD_TEST);
        logger.info("Resultado da comparação: fraudType={}, detailedReason={}", fraudType, detailedReason);

        neutralDetection.face.release();
        neutralDetection.alignedFace.release();
        neutralDetection.normalizedFace.release();
        smileDetection.face.release();
        smileDetection.alignedFace.release();
        smileDetection.normalizedFace.release();
        neutralImg.release();
        smileImg.release();

        return new ImageComparisonResult(areFacesEqual, areCoordinatesEqual, similarityScore, detailedReason,
                lat1, lon1, lat2, lon2, euclideanDistance, fraudType);
    }

    private static double calculateNorm(float[] embedding) {
        double norm = 0.0;
        for (float value : embedding) {
            norm += value * value;
        }
        return Math.sqrt(norm);
    }

    private static class FaceDetectionResult {
        Mat face;
        Mat alignedFace;
        Mat normalizedFace;
        Rect faceRect;

        public FaceDetectionResult(Mat face, Mat alignedFace, Mat normalizedFace, Rect faceRect) {
            this.face = face;
            this.alignedFace = alignedFace;
            this.normalizedFace = normalizedFace;
            this.faceRect = faceRect;
        }
    }

    private static FaceDetectionResult detectAndProcessFace(Mat img, String filePrefix) {
        double aspectRatio = (double) img.cols() / img.rows();
        int targetWidth = 300;
        int targetHeight = (int) (targetWidth / aspectRatio);
        Mat resizedImg = new Mat();
        Imgproc.resize(img, resizedImg, new Size(targetWidth, targetHeight));

        Mat blob = Dnn.blobFromImage(resizedImg, 1.0, new Size(targetWidth, targetHeight),
                new Scalar(104, 117, 123), true, false);
        faceDetector.setInput(blob);
        Mat detections = faceDetector.forward();

        int numDetections = detections.size(2);
        logger.debug("Número de detecções: {}", numDetections);

        if (numDetections <= 0) {
            logger.warn("Nenhuma detecção retornada pelo modelo SSD.");
            resizedImg.release();
            blob.release();
            return new FaceDetectionResult(null, null, null, null);
        }

        int originalCols = img.cols();
        int originalRows = img.rows();
        float scaleX = (float) originalCols / targetWidth;
        float scaleY = (float) originalRows / targetHeight;

        float bestConfidence = -1;
        Rect bestRect = null;

        Mat detectionMat = detections.reshape(1, numDetections);
        for (int i = 0; i < numDetections; i++) {
            float[] data = new float[7];
            detectionMat.get(i, 0, data);

            float confidence = data[2];
            if (confidence > CONFIDENCE_THRESHOLD && confidence > bestConfidence) {
                float x1 = data[3] * targetWidth * scaleX;
                float y1 = data[4] * targetHeight * scaleY;
                float x2 = data[5] * targetWidth * scaleX;
                float y2 = data[6] * targetHeight * scaleY;

                Rect rect = new Rect(
                        (int) Math.max(0, x1),
                        (int) Math.max(0, y1),
                        (int) Math.min(x2 - x1, originalCols - x1),
                        (int) Math.min(y2 - y1, originalRows - y1)
                );

                if (rect.width > 100 && rect.height > 100 &&
                        rect.x + rect.width <= originalCols &&
                        rect.y + rect.height <= originalRows) {
                    bestConfidence = confidence;
                    bestRect = rect;
                }
            }
        }
        detectionMat.release();
        resizedImg.release();
        blob.release();

        if (bestRect == null) {
            logger.warn("Nenhuma face detectada com confiança > {}", CONFIDENCE_THRESHOLD);
            return new FaceDetectionResult(null, null, null, null);
        }

        logger.debug("Face detectada com maior confiança ({}) em: {{{}, {}, {}x{}}}",
                bestConfidence, bestRect.x, bestRect.y, bestRect.width, bestRect.height);

        int faceSize = Math.max(bestRect.width, bestRect.height);
        int padding = (int) (faceSize * 0.3);
        int finalSize = faceSize + 2 * padding;

        finalSize = Math.min(finalSize, Math.min(originalCols, originalRows));

        int centerX = bestRect.x + bestRect.width / 2;
        int centerY = bestRect.y + bestRect.height / 2 - (int)(bestRect.height * 0.3);

        int paddedX = centerX - finalSize / 2;
        int paddedY = centerY - finalSize / 2;

        paddedX = Math.max(0, Math.min(paddedX, originalCols - finalSize));
        paddedY = Math.max(0, Math.min(paddedY, originalRows - finalSize));

        if (finalSize <= 0 || paddedX < 0 || paddedY < 0 ||
                paddedX + finalSize > originalCols || paddedY + finalSize > originalRows) {
            logger.warn("Retângulo de recorte inválido: x={}, y={}, size={}", paddedX, paddedY, finalSize);
            return new FaceDetectionResult(null, null, null, null);
        }

        Rect paddedRect = new Rect(paddedX, paddedY, finalSize, finalSize);
        logger.debug("Retângulo de recorte centralizado: x={}, y={}, size={}", paddedX, paddedY, finalSize);

        Mat face = new Mat(img, paddedRect);

        String logsPath = System.getProperty("user.dir") + File.separator + "logs" + File.separator;
        String croppedPath = logsPath + "cropped_" + filePrefix + ".jpg";
//        Imgcodecs.imwrite(croppedPath, face);
        logger.debug("Face recortada salva em: {}", croppedPath);

        Mat alignedFace = alignFace(face, bestRect);
        Mat normalizedFace = normalizeFace(alignedFace);

        String alignedPath = logsPath + "aligned_" + filePrefix + ".jpg";
        String normalizedPath = logsPath + "normalized_" + filePrefix + ".jpg";
//        Imgcodecs.imwrite(alignedPath, alignedFace);
//        Imgcodecs.imwrite(normalizedPath, normalizedFace);

        return new FaceDetectionResult(face, alignedFace, normalizedFace, bestRect);
    }

    private static Mat alignFace(Mat face, Rect faceRect) {
        Mat aligned = new Mat();
        int targetSize = 112;
        int faceWidth = face.cols();
        int faceHeight = face.rows();

        int cropSize = Math.max(faceWidth, faceHeight);
        int cropX = Math.max(0, (faceWidth - cropSize) / 2);
        int cropY = Math.max(0, (faceHeight - cropSize) / 2);
        cropSize = Math.min(cropSize, Math.min(faceWidth - cropX, faceHeight - cropY));

        if (cropSize <= 0) {
            logger.warn("Tamanho de corte inválido: cropSize={}", cropSize);
            Imgproc.resize(face, aligned, new Size(targetSize, targetSize));
            return aligned;
        }

        Rect cropRect = new Rect(cropX, cropY, cropSize, cropSize);
        if (cropRect.x + cropRect.width > faceWidth || cropRect.y + cropRect.height > faceHeight) {
            logger.warn("Falha ao centralizar face: cropRect inválido x={}, y={}, width={}, height={}",
                    cropRect.x, cropRect.y, cropRect.width, cropRect.height);
            Imgproc.resize(face, aligned, new Size(targetSize, targetSize));
        } else {
            Mat croppedFace = new Mat(face, cropRect);
            Imgproc.resize(croppedFace, aligned, new Size(targetSize, targetSize));
            croppedFace.release();
        }
        logger.debug("Face alinhada: width={}, height={}", aligned.cols(), aligned.rows());
        return aligned;
    }

    private static Mat normalizeFace(Mat face) {
        Mat normalized = new Mat();
        face.copyTo(normalized);

        Core.normalize(normalized, normalized, 0, 255, Core.NORM_MINMAX);
        logger.debug("Face normalizada: channels={}, mean={}", normalized.channels(), Core.mean(normalized));
        return normalized;
    }

    private static float[] getFaceEmbedding(Mat face) {
        Mat rgbFace = new Mat();
        if (face.channels() == 1) {
            Imgproc.cvtColor(face, rgbFace, Imgproc.COLOR_GRAY2RGB);
        } else if (face.channels() == 3) {
            Imgproc.cvtColor(face, rgbFace, Imgproc.COLOR_BGR2RGB);
        } else {
            logger.warn("Imagem com número inesperado de canais ({}), forçando conversão para RGB", face.channels());
            Imgproc.cvtColor(face, rgbFace, Imgproc.COLOR_BGR2RGB);
        }

        Mat preprocessedFace = new Mat();
        Imgproc.resize(rgbFace, preprocessedFace, new Size(112, 112));
        preprocessedFace.convertTo(preprocessedFace, CvType.CV_32FC3);

        Core.subtract(preprocessedFace, new Scalar(127.5, 127.5, 127.5), preprocessedFace);
        Core.divide(preprocessedFace, new Scalar(128.0, 128.0, 128.0), preprocessedFace);

        Mat blob = Dnn.blobFromImage(preprocessedFace, 1.0, new Size(112, 112), new Scalar(0, 0, 0), true, false);
        faceNet.setInput(blob);
        Mat embeddingMat = faceNet.forward();

        float[] embedding = new float[(int) embeddingMat.total()];
        embeddingMat.get(0, 0, embedding);

        rgbFace.release();
        preprocessedFace.release();
        blob.release();
        embeddingMat.release();

        return embedding;
    }

    private static double calculateSSIM(Mat img1, Mat img2) {
        Mat resized1 = new Mat();
        Mat resized2 = new Mat();
        Size commonSize = new Size(112, 112);
        Imgproc.resize(img1, resized1, commonSize);
        Imgproc.resize(img2, resized2, commonSize);

        Mat gray1 = new Mat();
        Mat gray2 = new Mat();
        Imgproc.cvtColor(resized1, gray1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(resized2, gray2, Imgproc.COLOR_BGR2GRAY);

        double C1 = Math.pow(0.01 * 255, 2);
        double C2 = Math.pow(0.03 * 255, 2);

        Scalar mean1 = Core.mean(gray1);
        Scalar mean2 = Core.mean(gray2);

        MatOfDouble stdDev1 = new MatOfDouble();
        MatOfDouble stdDev2 = new MatOfDouble();
        Core.meanStdDev(gray1, new MatOfDouble(), stdDev1);
        Core.meanStdDev(gray2, new MatOfDouble(), stdDev2);

        double mu1 = mean1.val[0];
        double mu2 = mean2.val[0];
        double sigma1 = stdDev1.toArray()[0];
        double sigma2 = stdDev2.toArray()[0];

        Mat temp1 = new Mat();
        Mat temp2 = new Mat();
        Core.subtract(gray1, Scalar.all(mu1), temp1);
        Core.subtract(gray2, Scalar.all(mu2), temp2);

        Mat multMat = new Mat();
        Core.multiply(temp1, temp2, multMat);
        Scalar covSum = Core.sumElems(multMat);
        double sigma12 = covSum.val[0] / (gray1.rows() * gray1.cols() - 1);

        double ssim = ((2 * mu1 * mu2 + C1) * (2 * sigma12 + C2)) /
                ((mu1 * mu1 + mu2 * mu2 + C1) * (sigma1 * sigma1 + sigma2 * sigma2 + C2));

        resized1.release();
        resized2.release();
        gray1.release();
        gray2.release();
        temp1.release();
        temp2.release();
        multMat.release();

        logger.debug("SSIM calculada: {}", ssim);
        return Math.max(0, Math.min(1, ssim));
    }

    private static double calculateEuclideanDistance(float[] emb1, float[] emb2) {
        if (emb1 == null || emb2 == null || emb1.length != emb2.length) {
            logger.error("Embeddings inválidos ou de tamanhos diferentes: emb1={}, emb2={}",
                    emb1 == null ? "null" : emb1.length, emb2 == null ? "null" : emb2.length);
            throw new IllegalArgumentException("Embeddings inválidos ou de tamanhos diferentes.");
        }

        double sum = 0;
        for (int i = 0; i < emb1.length; i++) {
            double diff = emb1[i] - emb2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}