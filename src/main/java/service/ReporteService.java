package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReporteService {

    // --- CLASE INTERNA PARA EL PIE DE PÁGINA ---
    class PieDePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaHora = "Documento generado el: " + dtf.format(LocalDateTime.now());

            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    new Phrase(fechaHora, new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC)),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }

    // --- MÉTODO PRINCIPAL PARA GENERAR EL PDF ---
    public void generarEstadoCuenta(int cuentaId) {
        Document documento = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream("Reporte_Cuenta_" + cuentaId + ".pdf"));

            // SE ACTIVA EL PIE DE PÁGINA
            writer.setPageEvent(new PieDePagina());

            documento.open();
            documento.add(new Paragraph("ESTADO DE CUENTA BANCARIO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            documento.add(new Paragraph(" ")); // Espacio en blanco

            documento.add(new Paragraph("Detalle de movimientos de la cuenta ID: " + cuentaId));

            documento.close();
            System.out.println("PDF generado con éxito en la carpeta raíz del proyecto.");
        } catch (Exception e) {
            System.err.println("Error al crear el PDF: " + e.getMessage());
        }
    }
}
