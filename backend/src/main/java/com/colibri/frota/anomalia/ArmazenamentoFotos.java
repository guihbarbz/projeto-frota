package com.colibri.frota.anomalia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ArmazenamentoFotos {

    private final Path pasta;

    public ArmazenamentoFotos(@Value("${frota.uploads.dir:uploads}") String dir) throws IOException {
        this.pasta = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(pasta);
    }

    /** Salva a foto com nome gerado pelo servidor e devolve esse nome. */
    public String salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum arquivo enviado");
        }
        try {
            byte[] inicio;
            try (InputStream in = arquivo.getInputStream()) {
                inicio = in.readNBytes(12);
            }
            String ext = extensao(inicio);
            if (ext == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Envie uma imagem JPEG, PNG ou WebP");
            }
            String nome = UUID.randomUUID() + "." + ext;
            try (InputStream in = arquivo.getInputStream()) {
                Files.copy(in, pasta.resolve(nome));
            }
            return nome;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao salvar a foto");
        }
    }

    public Resource carregar(String nome) {
        Path p = pasta.resolve(nome).normalize();
        if (!p.startsWith(pasta) || !Files.exists(p)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada");
        }
        return new FileSystemResource(p);
    }

    public static MediaType tipo(String nome) {
        if (nome.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (nome.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    // confere os primeiros bytes do arquivo (o tipo informado pelo cliente não é confiável)
    private String extensao(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }
}