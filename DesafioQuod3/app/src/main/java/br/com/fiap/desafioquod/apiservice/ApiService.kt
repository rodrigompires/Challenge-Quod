package br.com.fiap.desafioquod.apiservice

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class UploadResponse(
    @SerializedName("transacaoId")
    val transacaoId: String? = null,

    @SerializedName("documentType")
    val documentType: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("fraudType")
    val fraudType: String? = null,

    @SerializedName("similarityScore")
    val similarityScore: Double? = null,

    @SerializedName("areCoordinatesEqual")
    val areCoordinatesEqual: Boolean? = null,

    @SerializedName("euclideanDistance")
    val euclideanDistance: Double? = null,

    @SerializedName("deviceInfo")
    val deviceInfo: String? = null,

    @SerializedName("analysisReport")
    val analysisReport: String? = null,

    @SerializedName("docScore")
    val docScore: Double? = null,
)

data class BiometricResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("registered")
    val registered: Boolean,

    @SerializedName("analysisReport")
    val analysisReport: String? = null
)

interface ApiService {
    @Multipart
    @POST("/api/images/upload")
    suspend fun uploadImages(
        @Part image1: MultipartBody.Part,
        @Part image2: MultipartBody.Part,
        @Part("latitude1") latitude1: RequestBody,
        @Part("longitude1") longitude1: RequestBody,
        @Part("latitude2") latitude2: RequestBody,
        @Part("longitude2") longitude2: RequestBody,
        @Part("androidVersion") androidVersion: RequestBody,
        @Part("apiLevel") apiLevel: RequestBody,
        @Part("manufacturer") manufacturer: RequestBody?,
        @Part("model") model: RequestBody?,
        @Part("captureDate") captureDate: RequestBody?,
        @Part("testMode") testMode: RequestBody?
    ): Response<UploadResponse>

    // Novo endpoint para Documentoscopia (frente e verso)
    @Multipart
    @POST("/api/documents/upload")
    suspend fun uploadDocumentImages(
        @Part("documentType") documentType: String,
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
        @Part("latitudeFront") latitudeFront: Double?,
        @Part("longitudeFront") longitudeFront: Double?,
        @Part("latitudeBack") latitudeBack: Double?,
        @Part("longitudeBack") longitudeBack: Double?,
        @Part("androidVersion") androidVersion: String?,
        @Part("apiLevel") apiLevel: Int?,
        @Part("manufacturer") manufacturer: String?,
        @Part("model") model: String?,
        @Part("captureDate") captureDate: String?
    ): Response<UploadResponse>


    @Multipart
    @POST("/api/biometric/validate")
    suspend fun validateBiometric(
        @Part("authenticated") authenticated: RequestBody,
        @Part("failedAttempts") failedAttempts: RequestBody,
        @Part("deviceId") deviceId: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("androidVersion") androidVersion: RequestBody?,
        @Part("apiLevel") apiLevel: RequestBody?,
        @Part("manufacturer") manufacturer: RequestBody?,
        @Part("model") model: RequestBody?,
        @Part("captureDate") captureDate: RequestBody?
    ): Response<BiometricResponse>

}



