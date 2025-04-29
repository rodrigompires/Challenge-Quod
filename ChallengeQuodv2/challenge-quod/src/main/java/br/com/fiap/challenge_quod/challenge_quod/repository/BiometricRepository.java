package br.com.fiap.challenge_quod.challenge_quod.repository;


import br.com.fiap.challenge_quod.challenge_quod.model.BiometricModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BiometricRepository extends MongoRepository<BiometricModel, String> {
    BiometricModel findTopByDeviceIdOrderByDataCaptura24hDesc(String deviceId);
}