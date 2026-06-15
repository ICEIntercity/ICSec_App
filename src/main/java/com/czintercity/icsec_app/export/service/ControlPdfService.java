package com.czintercity.icsec_app.export.service;

import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Renders {@link Control}s to PDF documents using the standard Thymeleaf print template
 * ({@code control/controlPdf}) and the openhtmltopdf engine. The print template intentionally
 * omits the application navigation header so the output is suitable as a standalone document.
 */
@Service
public class ControlPdfService {
    private static final Logger log = LoggerFactory.getLogger(ControlPdfService.class);

    private static final String PDF_TEMPLATE = "control/controlPdf";

    private final ControlRepository controlRepository;
    private final SpringTemplateEngine templateEngine;

    public ControlPdfService(ControlRepository controlRepository, SpringTemplateEngine templateEngine) {
        this.controlRepository = controlRepository;
        this.templateEngine = templateEngine;
    }

    /**
     * Renders a single control to a PDF document.
     *
     * @param id the id of the control to render
     * @return the rendered PDF as a byte array
     * @throws java.util.NoSuchElementException if no control with the given id exists
     */
    @Transactional(readOnly = true)
    public byte[] renderControlPdf(UUID id) {
        Control control = controlRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Control with id " + id + " not found"));
        log.info("Rendering PDF for control {} ({}).", control.getCode(), id);
        return toPdf(control);
    }

    /**
     * Renders every control to its own PDF and bundles them into a single ZIP archive.
     *
     * @return the ZIP archive (containing one PDF per control) as a byte array
     */
    @Transactional(readOnly = true)
    public byte[] renderAllControlsZip() {
        log.info("Rendering PDF ZIP for all controls.");
        Set<String> usedNames = new HashSet<>();

        try (ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(zipBytes)) {

            int count = 0;
            for (Control control : controlRepository.findAll()) {
                byte[] pdf = toPdf(control);

                zip.putNextEntry(new ZipEntry(uniqueFileName(control, usedNames)));
                zip.write(pdf);
                zip.closeEntry();
                count++;
            }

            zip.finish();
            log.info("Built PDF ZIP with {} control(s).", count);
            return zipBytes.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build control PDF ZIP", e);
        }
    }

    /** Renders the standard print template for the given control and converts it to PDF bytes. */
    private byte[] toPdf(Control control) {
        Context context = new Context();
        context.setVariable("control", control);
        String html = templateEngine.process(PDF_TEMPLATE, context);

        // Thymeleaf emits HTML5; parse it and re-serialize as well-formed XML for the PDF renderer.
        Document jsoupDoc = Jsoup.parse(html);
        jsoupDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withW3cDocument(w3cDoc, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render PDF for control " + control.getId(), e);
        }
    }

    /** Builds a filesystem-safe, unique {@code <code>-<name>.pdf} file name for a control. */
    private String uniqueFileName(Control control, Set<String> usedNames) {
        String base = (control.getCode() + "-" + control.getName())
                .replaceAll("[^a-zA-Z0-9-_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        if (base.isEmpty()) {
            base = "control-" + control.getId();
        }

        String candidate = base + ".pdf";
        int suffix = 2;
        while (!usedNames.add(candidate)) {
            candidate = base + "-" + suffix++ + ".pdf";
        }
        return candidate;
    }
}