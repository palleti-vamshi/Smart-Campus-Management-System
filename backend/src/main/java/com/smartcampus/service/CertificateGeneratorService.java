package com.smartcampus.service;

import com.smartcampus.entity.DocumentRequest;
import com.smartcampus.exception.InvalidOperationException;
import com.smartcampus.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for generating and safely accessing digital certificates.
 * Produces valid PDF documents using a lightweight, dependency-free PDF writer.
 */
@Service
@Slf4j
public class CertificateGeneratorService {

    private final Path storageDirectory;

    public CertificateGeneratorService(@Value("${document.storage.path:./storage/documents}") String storagePath) {
        this.storageDirectory = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
            log.info("Digital document storage initialized at: {}", this.storageDirectory);
        } catch (IOException e) {
            log.error("Failed to initialize document storage directory at {}", this.storageDirectory, e);
            throw new IllegalStateException("Could not initialize document storage directory", e);
        }
    }

    /**
     * Generates a digital certificate PDF for the given request and saves it to storage.
     * Returns the absolute path string of the generated file.
     */
    public String generateCertificate(DocumentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Document request must not be null");
        }

        String safeFileName = "CERT-" + request.getRequestNumber().replaceAll("[^a-zA-Z0-9_-]", "_") + ".pdf";
        Path targetPath = storageDirectory.resolve(safeFileName).normalize();

        // Enforce path traversal protection
        if (!targetPath.startsWith(storageDirectory)) {
            throw new InvalidOperationException("Invalid target file path generated");
        }

        byte[] pdfBytes = buildCertificatePdf(request);

        try {
            Files.write(targetPath, pdfBytes);
            log.info("Certificate PDF successfully generated for request {} at {}", request.getRequestNumber(), targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            log.error("Failed to write certificate PDF for request {}", request.getRequestNumber(), e);
            throw new InvalidOperationException("Failed to generate digital certificate document: " + e.getMessage());
        }
    }

    /**
     * Resolves and validates a stored certificate file for download.
     * Prevents path traversal and checks file existence.
     */
    public Resource loadAsResource(String documentPath) {
        if (documentPath == null || documentPath.isBlank()) {
            throw new ResourceNotFoundException("Document file path is not set");
        }

        Path requestedFile = Paths.get(documentPath).toAbsolutePath().normalize();

        // Path traversal guard
        if (!requestedFile.startsWith(storageDirectory)) {
            log.warn("Path traversal attempt detected: {}", documentPath);
            throw new InvalidOperationException("Invalid document path access");
        }

        if (!Files.exists(requestedFile) || !Files.isReadable(requestedFile)) {
            log.error("Certificate file not found or unreadable on disk: {}", requestedFile);
            throw new ResourceNotFoundException("Digital document file not found on server");
        }

        return new FileSystemResource(requestedFile);
    }

    /**
     * Builds a formatted PDF 1.4 document containing certificate details.
     */
    private byte[] buildCertificatePdf(DocumentRequest request) {
        String deptName = "DEPARTMENT OF COMPUTER SCIENCE & ENGINEERING";
        if (request.getStudent() != null
                && request.getStudent().getProgram() != null
                && request.getStudent().getProgram().getDepartment() != null) {
            deptName = request.getStudent().getProgram().getDepartment().getDepartmentName().toUpperCase();
        }

        String studentName = request.getStudent() != null
                ? (request.getStudent().getFirstName() + " " + (request.getStudent().getLastName() != null ? request.getStudent().getLastName() : "")).trim()
                : "N/A";
        String rollNumber = request.getStudent() != null ? request.getStudent().getRollNumber() : "N/A";
        String programName = request.getStudent() != null && request.getStudent().getProgram() != null
                ? request.getStudent().getProgram().getProgramName()
                : "N/A";

        String docType = request.getDocumentType() != null ? request.getDocumentType().getDocumentName() : "DIGITAL CERTIFICATE";
        String purpose = request.getPurpose() != null ? request.getPurpose() : "Official Records";
        String reqNumber = request.getRequestNumber();
        String verificationCode = request.getVerificationCode() != null ? request.getVerificationCode() : "N/A";
        String issueDate = request.getIssuedAt() != null
                ? request.getIssuedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(java.time.LocalDateTime.now());

        // Construct PDF content stream
        StringBuilder content = new StringBuilder();

        // Draw decorative outer border (margin 36 pt = 0.5 in)
        content.append("q\n");
        content.append("0.5 w\n");
        content.append("0.2 0.3 0.5 RG\n"); // Navy border
        content.append("36 36 540 720 re\n");
        content.append("S\n");
        content.append("1.5 w\n");
        content.append("40 40 532 712 re\n");
        content.append("S\n");
        content.append("Q\n");

        // Header Title
        content.append("BT\n");
        content.append("/F1 20 Tf\n");
        content.append("0.1 0.2 0.4 rg\n"); // Deep navy
        content.append("100 700 Td\n");
        content.append("(").append(escapePdf("SMART CAMPUS MANAGEMENT SYSTEM")).append(") Tj\n");
        content.append("ET\n");

        // Department Name
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append("0.3 0.3 0.3 rg\n");
        content.append("100 680 Td\n");
        content.append("(").append(escapePdf(deptName)).append(") Tj\n");
        content.append("ET\n");

        // Divider Line
        content.append("q\n");
        content.append("1 w\n");
        content.append("0.7 0.7 0.7 RG\n");
        content.append("100 665 m 512 665 l S\n");
        content.append("Q\n");

        // Certificate Name
        content.append("BT\n");
        content.append("/F1 16 Tf\n");
        content.append("0.15 0.35 0.65 rg\n");
        content.append("100 635 Td\n");
        content.append("(").append(escapePdf("OFFICIAL CERTIFICATE: " + docType.replace('_', ' '))).append(") Tj\n");
        content.append("ET\n");

        // Certificate Body Intro
        content.append("BT\n");
        content.append("/F2 11 Tf\n");
        content.append("0.1 0.1 0.1 rg\n");
        content.append("100 595 Td\n");
        content.append("(This is an official document issued by the Smart Campus Academic Authority.) Tj\n");
        content.append("0 -20 Td\n");
        content.append("(It certifies the following department academic record and credentials:) Tj\n");
        content.append("ET\n");

        // Data Fields Table
        String[][] fields = new String[][]{
                {"Student Name:", studentName},
                {"Roll Number:", rollNumber},
                {"Academic Program:", programName},
                {"Document Type:", docType},
                {"Stated Purpose:", purpose},
                {"Request Number:", reqNumber},
                {"Issue Date & Time:", issueDate},
                {"Verification Code:", verificationCode}
        };

        int yPos = 530;
        for (String[] field : fields) {
            content.append("BT\n");
            content.append("/F1 10 Tf\n");
            content.append("0.2 0.2 0.2 rg\n");
            content.append("100 ").append(yPos).append(" Td\n");
            content.append("(").append(escapePdf(field[0])).append(") Tj\n");
            content.append("ET\n");

            content.append("BT\n");
            if (field[0].contains("Verification")) {
                content.append("/F3 11 Tf\n");
                content.append("0.7 0.1 0.1 rg\n"); // Red/distinct for verification code
            } else {
                content.append("/F2 10 Tf\n");
                content.append("0.1 0.1 0.1 rg\n");
            }
            content.append("230 ").append(yPos).append(" Td\n");
            content.append("(").append(escapePdf(field[1])).append(") Tj\n");
            content.append("ET\n");

            yPos -= 22;
        }

        // Verification Info Box
        int boxY = yPos - 30;
        content.append("q\n");
        content.append("0.5 w\n");
        content.append("0.85 0.90 0.95 rg\n"); // Light blue background
        content.append("100 ").append(boxY).append(" 412 55 re f\n");
        content.append("0.3 0.5 0.7 RG\n");
        content.append("100 ").append(boxY).append(" 412 55 re S\n");
        content.append("Q\n");

        content.append("BT\n");
        content.append("/F1 9 Tf\n");
        content.append("0.1 0.2 0.4 rg\n");
        content.append("110 ").append(boxY + 38).append(" Td\n");
        content.append("(DIGITAL VERIFICATION NOTICE) Tj\n");
        content.append("/F2 8 Tf\n");
        content.append("0.2 0.2 0.2 rg\n");
        content.append("0 -14 Td\n");
        content.append("(This digitally signed document can be publicly validated on the institution portal using) Tj\n");
        content.append("0 -12 Td\n");
        content.append("(Verification Code: ").append(escapePdf(verificationCode)).append(" at /api/public/documents/verify/").append(escapePdf(verificationCode)).append(") Tj\n");
        content.append("ET\n");

        // Footer
        content.append("BT\n");
        content.append("/F2 8 Tf\n");
        content.append("0.5 0.5 0.5 rg\n");
        content.append("100 80 Td\n");
        content.append("(Generated automatically by Smart Campus Management System. No physical signature required.) Tj\n");
        content.append("ET\n");

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);

        // Assemble standard PDF objects
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            List<Long> offsets = new ArrayList<>();

            // Header
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
            out.write(new byte[]{'%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});

            // Obj 1: Catalog
            offsets.add((long) out.size());
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 2: Pages
            offsets.add((long) out.size());
            out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 3: Page
            offsets.add((long) out.size());
            String pageObj = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                    + "/Resources << /Font << /F1 4 0 R /F2 5 0 R /F3 6 0 R >> >> "
                    + "/Contents 7 0 R >>\nendobj\n";
            out.write(pageObj.getBytes(StandardCharsets.ISO_8859_1));

            // Obj 4: Helvetica-Bold
            offsets.add((long) out.size());
            out.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 5: Helvetica
            offsets.add((long) out.size());
            out.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 6: Courier-Bold
            offsets.add((long) out.size());
            out.write("6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 7: Content Stream
            offsets.add((long) out.size());
            String streamHeader = "7 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n";
            out.write(streamHeader.getBytes(StandardCharsets.ISO_8859_1));
            out.write(streamBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Xref
            long startXref = out.size();
            out.write("xref\n".getBytes(StandardCharsets.ISO_8859_1));
            out.write(("0 " + (offsets.size() + 1) + "\n").getBytes(StandardCharsets.ISO_8859_1));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (Long offset : offsets) {
                out.write(String.format("%010d 00000 n \n", offset).getBytes(StandardCharsets.ISO_8859_1));
            }

            // Trailer
            out.write(("trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\n").getBytes(StandardCharsets.ISO_8859_1));
            out.write("startxref\n".getBytes(StandardCharsets.ISO_8859_1));
            out.write((startXref + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

            return out.toByteArray();
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to construct certificate PDF bytes: " + e.getMessage());
        }
    }

    private String escapePdf(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
