package com.bank.service;

import com.bank.model.Transaccion;
import com.bank.repository.TransaccionRepository; // Corregido: ya no es src.repository
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    // --- CLASE INTERNA PARA EL PIE DE PÁGINA ---
    class PieDePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaHora = "Documento oficial generado el: " + dtf.format(LocalDateTime.now());

            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    new Phrase(fechaHora, new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }

    // --- MÉTODO PRINCIPAL PARA GENERAR EL PDF ---
    public void generarEstadoCuenta(Long cuentaId) {
        // Buscamos las transacciones reales en la DB
        List<Transaccion> movimientos = transaccionRepository.findByCuentaIdOrderByFechaHoraDesc(cuentaId);

        Document documento = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream("Estado_Cuenta_" + cuentaId + ".pdf"));
            writer.setPageEvent(new PieDePagina());

            documento.open();

            // 1. Encabezado Estilizado
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Paragraph titulo = new Paragraph("ESTADO DE CUENTA BANCARIO", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph(" "));

            // 2. Información General
            documento.add(new Paragraph("ID de Cuenta: " + cuentaId));
            documento.add(new Paragraph("Número de Movimientos: " + movimientos.size()));
            documento.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
            documento.add(new Paragraph(" "));

            // 3. Tabla de Movimientos
            PdfPTable tabla = new PdfPTable(3); // 3 columnas: Fecha, Tipo, Monto
            tabla.setWidthPercentage(100);

            // Estilo de Cabecera de Tabla
            Font fuenteCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            PdfPCell celdaH1 = new PdfPCell(new Phrase("Fecha y Hora", fuenteCabecera));
            PdfPCell celdaH2 = new PdfPCell(new Phrase("Tipo de Operación", fuenteCabecera));
            PdfPCell celdaH3 = new PdfPCell(new Phrase("Monto ($)", fuenteCabecera));

            BaseColor colorBanco = new BaseColor(0, 51, 102); // Azul Marino
            celdaH1.setBackgroundColor(colorBanco);
            celdaH2.setBackgroundColor(colorBanco);
            celdaH3.setBackgroundColor(colorBanco);

            tabla.addCell(celdaH1);
            tabla.addCell(celdaH2);
            tabla.addCell(celdaH3);

            // 4. Llenado de datos reales
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Transaccion t : movimientos) {
                tabla.addCell(t.getFechaHora().format(formatter));
                tabla.addCell(t.getTipo());

                // Color según el tipo (Rojo para retiros, Verde para depósitos)
                PdfPCell celdaMonto = new PdfPCell(new Phrase(String.format("%.2f", t.getMonto())));
                if (t.getTipo().contains("RETIRO") || t.getTipo().contains("SALIDA")) {
                    celdaMonto.setBackgroundColor(new BaseColor(255, 230, 230));
                }
                tabla.addCell(celdaMonto);
            }

            documento.add(tabla);
            documento.close();

            System.out.println("PDF: Estado de cuenta " + cuentaId + " generado con datos reales de la DB.");

        } catch (Exception e) {
            System.err.println("Error crítico al generar PDF: " + e.getMessage());
        }
    }
}
