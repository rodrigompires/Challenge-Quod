package br.com.fiap.challenge_quod.challenge_quod.controller;

import br.com.fiap.challenge_quod.challenge_quod.dto.DocumentRequestDTO;
import br.com.fiap.challenge_quod.challenge_quod.dto.DocumentResponseDTO;
import br.com.fiap.challenge_quod.challenge_quod.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponseDTO> uploadDocument(@Valid @ModelAttribute DocumentRequestDTO requestDTO) {
        DocumentResponseDTO response = documentService.validateAndSaveDocument(requestDTO);
        return ResponseEntity.ok(response);
    }
}