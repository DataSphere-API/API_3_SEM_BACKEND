package com.datasphere.fusex.atendimento.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GerarImagemQrCodeServiceTest {

    @Test
    void imagemPngPodeSerDecodificadaEContemExatamenteOCodigo() throws Exception {
        String codigo = new GerarCodigoUnicoQrService().gerarCodigoUnicoQr();
        String resposta = new GerarImagemQrCodeService().gerarImagemQrCode(codigo);

        String prefixo = "data:image/png;base64,";
        assertTrue(resposta.startsWith(prefixo));
        byte[] png = Base64.getDecoder().decode(resposta.substring(prefixo.length()));
        assertEquals(0x89, Byte.toUnsignedInt(png[0]));
        assertEquals("PNG", new String(png, 1, 3, java.nio.charset.StandardCharsets.US_ASCII));
        var imagem = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(imagem);
        assertEquals(300, imagem.getWidth());
        assertEquals(300, imagem.getHeight());
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(imagem)));
        assertEquals(codigo, new MultiFormatReader().decode(bitmap).getText());
    }

    @Test
    void codigoJaExistenteDoSeedTambemGeraImagemLegivel() throws Exception {
        String codigo = "QR-TESTE-PSICO-001";
        String imagemBase64 = new GerarImagemQrCodeService().gerarImagemQrCode(codigo);
        var imagem = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder()
                .decode(imagemBase64.substring(imagemBase64.indexOf(',') + 1))));
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(imagem)));
        assertEquals(codigo, new MultiFormatReader().decode(bitmap).getText());
    }
}
