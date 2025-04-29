package br.com.fiap.challenge_quod.challenge_quod.repository;

import br.com.fiap.challenge_quod.challenge_quod.model.FacialBiometricDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacialBiometricRepository extends MongoRepository<FacialBiometricDocument, String> {

}
