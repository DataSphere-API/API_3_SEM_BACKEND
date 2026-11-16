package com.datasphere.fusex.atendimento.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class GerarImagemQrCodeService {

    private static final int TAMANHO = 300;

    public String gerarImagemQrCode(String codigo) {
        try (ByteArrayOutputStream imagem = new ByteArrayOutputStream()) {
            var matriz = new QRCodeWriter().encode(codigo, BarcodeFormat.QR_CODE, TAMANHO, TAMANHO,
                    Map.of(EncodeHintType.CHARACTER_SET, "UTF-8"));
            MatrixToImageWriter.writeToStream(matriz, "PNG", imagem);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imagem.toByteArray());
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Não foi possível gerar a imagem do QR Code.", ex);
        }
    }
}
