package com.colibri.frota.anomalia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class ArmazenamentoFotos {

    public record Imagem(byte[] dados, String tipo) {
    }

    public Imagem ler(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum arquivo enviado");
        }
        try {
            byte[] dados = arquivo.getBytes();
            String tipo = tipoPelaAssinatura(dados);
            if (tipo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Envie uma imagem JPEG, PNG ou WebP");
            }
            return new Imagem(dados, tipo);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao ler a foto");
        }
    }

    // confere os primeiros bytes do arquivo (o tipo informado pelo cliente não é confiável)
    private String tipoPelaAssinatura(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}