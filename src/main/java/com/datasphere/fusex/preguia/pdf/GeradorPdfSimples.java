package com.datasphere.fusex.preguia.pdf;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeradorPdfSimples {

    private static final int LINHAS_POR_PAGINA = 52;

    private GeradorPdfSimples() {
    }

    public static byte[] gerar(List<String> linhas) {
        List<List<String>> paginas = separarEmPaginas(linhas);
        int totalPaginas = paginas.size();

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        List<Integer> posicoes = new ArrayList<>();

        escrever(saida, "%PDF-1.4\n");

        posicoes.add(saida.size());
        escrever(saida, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        StringBuilder filhos = new StringBuilder();
        for (int i = 0; i < totalPaginas; i++) {
            filhos.append(4 + 2 * i).append(" 0 R ");
        }
        posicoes.add(saida.size());
        escrever(saida, "2 0 obj\n<< /Type /Pages /Kids [" + filhos + "] /Count " + totalPaginas + " >>\nendobj\n");

        posicoes.add(saida.size());
        escrever(saida, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>\nendobj\n");

        for (int i = 0; i < totalPaginas; i++) {
            int numeroPagina = 4 + 2 * i;
            int numeroConteudo = 5 + 2 * i;
            String conteudo = montarConteudo(paginas.get(i));

            posicoes.add(saida.size());
            escrever(saida, numeroPagina + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + numeroConteudo + " 0 R >>\nendobj\n");

            posicoes.add(saida.size());
            escrever(saida, numeroConteudo + " 0 obj\n<< /Length " + conteudo.getBytes(StandardCharsets.ISO_8859_1).length
                    + " >>\nstream\n" + conteudo + "\nendstream\nendobj\n");
        }

        int inicioXref = saida.size();
        int totalObjetos = posicoes.size() + 1;
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(totalObjetos).append("\n");
        xref.append("0000000000 65535 f \n");
        for (int posicao : posicoes) {
            xref.append(String.format("%010d 00000 n \n", posicao));
        }
        xref.append("trailer\n<< /Size ").append(totalObjetos).append(" /Root 1 0 R >>\n");
        xref.append("startxref\n").append(inicioXref).append("\n%%EOF\n");
        escrever(saida, xref.toString());

        return saida.toByteArray();
    }

    private static List<List<String>> separarEmPaginas(List<String> linhas) {
        List<List<String>> paginas = new ArrayList<>();
        List<String> atual = new ArrayList<>();
        for (String linha : linhas) {
            if (atual.size() == LINHAS_POR_PAGINA) {
                paginas.add(atual);
                atual = new ArrayList<>();
            }
            atual.add(linha);
        }
        paginas.add(atual);
        return paginas;
    }

    private static String montarConteudo(List<String> linhas) {
        StringBuilder conteudo = new StringBuilder();
        conteudo.append("BT\n/F1 10 Tf\n14 TL\n50 790 Td\n");
        for (String linha : linhas) {
            conteudo.append("(").append(escapar(linha)).append(") Tj T*\n");
        }
        conteudo.append("ET");
        return conteudo.toString();
    }

    private static String escapar(String texto) {
        StringBuilder resultado = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (c == '\\' || c == '(' || c == ')') {
                resultado.append('\\').append(c);
            } else if (c > 255) {
                resultado.append('?');
            } else {
                resultado.append(c);
            }
        }
        return resultado.toString();
    }

    private static void escrever(ByteArrayOutputStream saida, String texto) {
        byte[] bytes = texto.getBytes(StandardCharsets.ISO_8859_1);
        saida.write(bytes, 0, bytes.length);
    }
}