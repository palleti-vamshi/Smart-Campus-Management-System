package com.smartcampus.service;

import com.smartcampus.entity.DocumentRequest;
import com.smartcampus.entity.Student;
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
 * Produces valid A4 PDF documents following the authentic VNR VJIET institutional certificate layout.
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
     * Checks if a document path corresponds to an existing, readable file within the storage directory.
     */
    public boolean hasValidFile(String documentPath) {
        if (documentPath == null || documentPath.isBlank()) {
            return false;
        }
        try {
            Path requestedFile = Paths.get(documentPath).toAbsolutePath().normalize();
            if (!requestedFile.startsWith(storageDirectory)) {
                return false;
            }
            return Files.exists(requestedFile) && Files.isReadable(requestedFile);
        } catch (Exception e) {
            return false;
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
     * Builds a formatted single-page A4 PDF 1.4 document following the authentic
     * VNR VJIET institutional Bonafide Certificate layout.
     */
    private byte[] buildCertificatePdf(DocumentRequest request) {
        Student student = request.getStudent();

        String studentName = "STUDENT";
        if (student != null) {
            String first = student.getFirstName() != null ? student.getFirstName().trim() : "";
            String last = student.getLastName() != null ? student.getLastName().trim() : "";
            studentName = (first + " " + last).trim().toUpperCase();
            if (studentName.isEmpty()) {
                studentName = "STUDENT";
            }
        }

        String prefix = "Mr./Ms.";
        String pronoun = "their";
        if (student != null && student.getGender() != null) {
            if ("MALE".equalsIgnoreCase(student.getGender())) {
                prefix = "Mr.";
                pronoun = "his";
            } else if ("FEMALE".equalsIgnoreCase(student.getGender())) {
                prefix = "Ms.";
                pronoun = "her";
            }
        }

        String rollNumber = (student != null && student.getRollNumber() != null)
                ? student.getRollNumber().trim()
                : "N/A";

        String branchDisplay = (student != null && student.getProgram() != null)
                ? getBranchDisplayName(student.getProgram().getProgramCode(), student.getProgram().getProgramName())
                : "CSE - ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING";

        int sem = (student != null && student.getCurrentSemester() != null && student.getCurrentSemester() > 0)
                ? student.getCurrentSemester()
                : 3;
        String yearAndSem = getYearAndSemesterText(sem);

        int admYear = (student != null && student.getAdmissionYear() != null && student.getAdmissionYear() >= 2020)
                ? student.getAdmissionYear()
                : 2024;
        String academicYear = getAcademicYear(admYear, sem);

        String docTypeRaw = (request.getDocumentType() != null && request.getDocumentType().getDocumentName() != null)
                ? request.getDocumentType().getDocumentName().trim().toUpperCase()
                : "BONAFIDE_CERTIFICATE";

        String certTitle = "BONAFIDE CERTIFICATE";
        if (docTypeRaw.contains("TRANSCRIPT")) {
            certTitle = "ACADEMIC TRANSCRIPT";
        } else if (docTypeRaw.contains("INTERNSHIP")) {
            certTitle = "INTERNSHIP NO OBJECTION CERTIFICATE";
        } else if (docTypeRaw.contains("COURSE_COMPLETION")) {
            certTitle = "COURSE COMPLETION CERTIFICATE";
        }

        String rawPurpose = (request.getPurpose() != null && !request.getPurpose().isBlank())
                ? request.getPurpose().trim()
                : "Scholarship";

        String purposeClean = rawPurpose;
        if (purposeClean.endsWith(".")) {
            purposeClean = purposeClean.substring(0, purposeClean.length() - 1).trim();
        }

        String purposeSentence;
        if (purposeClean.toLowerCase().endsWith("purpose")) {
            purposeSentence = "This certificate is issued at " + pronoun + " request for " + purposeClean + ".";
        } else if (purposeClean.equalsIgnoreCase("scholarship")
                || purposeClean.equalsIgnoreCase("passport")
                || purposeClean.equalsIgnoreCase("visa")
                || (purposeClean.length() < 25 && !purposeClean.toLowerCase().contains("loan")
                    && !purposeClean.toLowerCase().contains("reimbursement")
                    && !purposeClean.toLowerCase().contains("application"))) {
            purposeSentence = "This certificate is issued at " + pronoun + " request for " + purposeClean + " purpose.";
        } else {
            purposeSentence = "This certificate is issued at " + pronoun + " request for " + purposeClean + ".";
        }

        String formattedDate = getFormattedIssueDate(request.getIssuedAt());
        String verificationCode = (request.getVerificationCode() != null && !request.getVerificationCode().isBlank())
                ? request.getVerificationCode().trim()
                : "SCMS-2026-V0052";

        // Construct PDF content stream
        // Standard A4 dimensions: 595 pt width x 842 pt height
        // Page bounds: [0 0 595 842]
        StringBuilder content = new StringBuilder();

        // 1. Institutional Header Block
        // Left Shield / Emblem placeholder for VNRVJIET
        content.append("q\n");
        content.append("1.5 w\n");
        content.append("0.050 0.140 0.380 RG\n"); // Deep Navy
        content.append("0.960 0.970 0.990 rg\n"); // Subtle tinted background
        content.append("55 732 64 68 re B\n");
        content.append("0.6 w\n");
        content.append("0.700 0.550 0.200 RG\n"); // Gold inner frame
        content.append("58 735 58 62 re S\n");
        content.append("Q\n");

        // Emblem Text
        drawCenteredText(content, "VNRVJIET", "/F1", 10.5, 0.050, 0.140, 0.380, 87, 776);
        drawCenteredText(content, "Estd. 1995", "/F2", 7.0, 0.400, 0.400, 0.450, 87, 762);
        drawCenteredText(content, "AUTONOMOUS", "/F1", 6.5, 0.700, 0.550, 0.200, 87, 748);

        // Institution Name & Accreditation Heading
        drawCenteredText(content, "VALLURUPALLI NAGESWARA RAO", "/F1", 15.0, 0.050, 0.140, 0.380, 332, 786);
        drawCenteredText(content, "VIGNANA JYOTHI INSTITUTE OF", "/F1", 15.0, 0.050, 0.140, 0.380, 332, 768);
        drawCenteredText(content, "ENGINEERING & TECHNOLOGY", "/F1", 15.0, 0.050, 0.140, 0.380, 332, 750);
        drawCenteredText(content, "AN AUTONOMOUS INSTITUTION", "/F1", 9.0, 0.650, 0.450, 0.150, 332, 735);
        drawCenteredText(content, "Approved by AICTE, New Delhi & Govt. of T.S., Affiliated to JNTUH, Hyderabad", "/F2", 7.5, 0.300, 0.300, 0.350, 332, 723);
        drawCenteredText(content, "Accredited by NAAC with 'A++' Grade & NBA (Tier-1), ISO 9001:2015 Certified", "/F2", 7.5, 0.300, 0.300, 0.350, 332, 712);

        // Header Ornamental Double Divider Lines
        content.append("q\n");
        content.append("1.5 w\n");
        content.append("0.050 0.140 0.380 RG\n"); // Navy
        content.append("55 700 m 540 700 l S\n");
        content.append("0.6 w\n");
        content.append("0.700 0.550 0.200 RG\n"); // Gold
        content.append("55 697 m 540 697 l S\n");
        content.append("Q\n");

        // 2. Issue Date Position (Top-Right)
        drawText(content, "Date: " + formattedDate, "/F1", 10.0, 0.150, 0.150, 0.200, 435, 672);

        // 3. Centered Formal Title & Underline
        drawCenteredText(content, certTitle, "/F1", 17.0, 0.050, 0.140, 0.380, 297.5, 625);
        content.append("q\n");
        content.append("1.2 w\n");
        content.append("0.050 0.140 0.380 RG\n");
        content.append("185 615 m 410 615 l S\n");
        content.append("0.5 w\n");
        content.append("0.700 0.550 0.200 RG\n");
        content.append("210 612 m 385 612 l S\n");
        content.append("Q\n");

        // 4. Main Certificate Paragraph & Content
        content.append("BT\n");
        content.append("65 560 Td\n");
        // Line 1: Certification intro + Name
        content.append("/F2 12 Tf 0.100 0.100 0.150 rg (This is to certify that ")
                .append(escapePdf(prefix)).append(" ) Tj ")
                .append("/F1 12.5 Tf 0.050 0.140 0.380 rg (")
                .append(escapePdf(studentName)).append(") Tj\n");

        // Line 2: Bonafide student of this Institute pursuing B.Tech.,
        content.append("0 -28 Td\n");
        content.append("/F2 12 Tf 0.100 0.100 0.150 rg (is a bonafide student of this Institute pursuing ) Tj ")
                .append("/F1 12.5 Tf 0.050 0.140 0.380 rg (B.Tech.,) Tj\n");

        // Line 3: Year Semester + Formal Branch
        content.append("0 -28 Td\n");
        content.append("/F1 12 Tf 0.050 0.140 0.380 rg (")
                .append(escapePdf(yearAndSem)).append(") Tj ")
                .append("/F2 12 Tf 0.100 0.100 0.150 rg ( in ) Tj ")
                .append("/F1 12 Tf 0.050 0.140 0.380 rg (")
                .append(escapePdf(branchDisplay)).append(") Tj\n");

        // Line 4: H.T. No. + Academic Year
        content.append("0 -28 Td\n");
        content.append("/F2 12 Tf 0.100 0.100 0.150 rg (branch with H.T. No. ) Tj ")
                .append("/F1 12.5 Tf 0.050 0.140 0.380 rg (")
                .append(escapePdf(rollNumber)).append(") Tj ")
                .append("/F2 12 Tf 0.100 0.100 0.150 rg ( during the academic year ) Tj ")
                .append("/F1 12 Tf 0.050 0.140 0.380 rg (")
                .append(escapePdf(academicYear)).append(".) Tj\n");

        // 5. Purpose of Issue
        List<String> purposeLines = wrapText(purposeSentence, 75);
        content.append("0 -45 Td\n");
        if (!purposeLines.isEmpty()) {
            content.append("/F2 11.5 Tf 0.120 0.120 0.180 rg (")
                    .append(escapePdf(purposeLines.get(0))).append(") Tj\n");
            for (int i = 1; i < purposeLines.size(); i++) {
                content.append("0 -20 Td\n");
                content.append("/F2 11.5 Tf 0.120 0.120 0.180 rg (")
                        .append(escapePdf(purposeLines.get(i))).append(") Tj\n");
            }
        }

        // 6. Conduct & Character
        content.append("0 -28 Td\n");
        content.append("/F2 11 Tf 0.150 0.150 0.200 rg (During the period of study at this Institute, ")
                .append(escapePdf(pronoun))
                .append(" conduct and character have been found to be GOOD.) Tj\n");
        content.append("ET\n");

        // 7. Circular Institutional Seal Placeholder (Lower-Middle/Left)
        content.append("q\n");
        content.append("1.2 w\n");
        content.append("0.180 0.280 0.480 RG\n"); // Slate Navy
        content.append(circleBeziers(165, 270, 46));
        content.append("0.6 w\n");
        content.append("[3 2] 0 d\n"); // Dashed concentric ring
        content.append(circleBeziers(165, 270, 41));
        content.append("Q\n");

        drawCenteredText(content, "VNR VJIET", "/F1", 8.5, 0.180, 0.280, 0.480, 165, 285);
        drawCenteredText(content, "ESTD 1995", "/F2", 7.0, 0.300, 0.400, 0.550, 165, 272);
        drawCenteredText(content, "INSTITUTION SEAL", "/F1", 7.5, 0.180, 0.280, 0.480, 165, 260);
        drawCenteredText(content, "HYDERABAD", "/F2", 6.5, 0.300, 0.400, 0.550, 165, 248);

        // 8. Principal Signature Area (Bottom-Right)
        content.append("q\n");
        content.append("0.8 w\n");
        content.append("0.600 0.600 0.650 RG\n");
        content.append("405 270 m 525 270 l S\n"); // Signature line
        content.append("Q\n");

        drawCenteredText(content, "Principal", "/F1", 11.5, 0.080, 0.140, 0.320, 465, 252);
        drawCenteredText(content, "VNR VJIET", "/F2", 8.5, 0.300, 0.300, 0.350, 465, 238);

        // 9. Document Footer & Official Address
        content.append("q\n");
        content.append("0.6 w\n");
        content.append("0.750 0.750 0.800 RG\n");
        content.append("55 90 m 540 90 l S\n");
        content.append("Q\n");

        drawCenteredText(content, "Vignana Jyothi Nagar, Pragathi Nagar, Nizampet (S.O), Hyderabad - 500 090, Telangana, India", "/F2", 8.0, 0.350, 0.350, 0.400, 297.5, 75);
        drawCenteredText(content, "Verification Code: " + verificationCode + "  |  Online Validation: /api/public/documents/verify/" + verificationCode, "/F2", 6.8, 0.500, 0.500, 0.550, 297.5, 62);

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);

        // Assemble standard PDF 1.4 objects
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

            // Obj 3: Page (A4 Portrait: 595 x 842 points)
            offsets.add((long) out.size());
            String pageObj = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> "
                    + "/Contents 6 0 R >>\nendobj\n";
            out.write(pageObj.getBytes(StandardCharsets.ISO_8859_1));

            // Obj 4: Helvetica-Bold
            offsets.add((long) out.size());
            out.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 5: Helvetica
            offsets.add((long) out.size());
            out.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 6: Content Stream
            offsets.add((long) out.size());
            String streamHeader = "6 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n";
            out.write(streamHeader.getBytes(StandardCharsets.ISO_8859_1));
            out.write(streamBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Xref table
            long startXref = out.size();
            out.write("xref\n".getBytes(StandardCharsets.ISO_8859_1));
            out.write(("0 " + (offsets.size() + 1) + "\n").getBytes(StandardCharsets.ISO_8859_1));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (Long offset : offsets) {
                out.write(String.format(java.util.Locale.US, "%010d 00000 n \n", offset).getBytes(StandardCharsets.ISO_8859_1));
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

    private String getBranchDisplayName(String programCode, String programName) {
        if (programCode != null) {
            String code = programCode.trim().toUpperCase();
            if (code.equals("AIML") || code.contains("AIML")) {
                return "CSE - ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING";
            }
            if (code.equals("IOT") || code.contains("IOT")) {
                return "CSE - INTERNET OF THINGS";
            }
            if (code.equals("RAI") || code.contains("RAI")) {
                return "ROBOTICS & ARTIFICIAL INTELLIGENCE";
            }
        }
        if (programName != null && !programName.isBlank()) {
            String lower = programName.toLowerCase();
            if (lower.contains("artificial intelligence") || lower.contains("aiml")) {
                return "CSE - ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING";
            }
            if (lower.contains("internet of things") || lower.contains("iot")) {
                return "CSE - INTERNET OF THINGS";
            }
            if (lower.contains("robotics") || lower.contains("rai")) {
                return "ROBOTICS & ARTIFICIAL INTELLIGENCE";
            }
            return programName.toUpperCase();
        }
        return "CSE - ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING";
    }

    private String getYearAndSemesterText(int sem) {
        String year;
        String semInYear;
        switch (sem) {
            case 1: year = "I Year"; semInYear = "Semester I"; break;
            case 2: year = "I Year"; semInYear = "Semester II"; break;
            case 3: year = "II Year"; semInYear = "Semester III"; break;
            case 4: year = "II Year"; semInYear = "Semester IV"; break;
            case 5: year = "III Year"; semInYear = "Semester V"; break;
            case 6: year = "III Year"; semInYear = "Semester VI"; break;
            case 7: year = "IV Year"; semInYear = "Semester VII"; break;
            case 8: year = "IV Year"; semInYear = "Semester VIII"; break;
            default: year = "II Year"; semInYear = "Semester III"; break;
        }
        return year + " " + semInYear;
    }

    private String getAcademicYear(int admissionYear, int currentSemester) {
        int yearOfStudy = (currentSemester + 1) / 2;
        int startYear = admissionYear + (yearOfStudy - 1);
        int endYear = startYear + 1;
        return startYear + "-" + endYear;
    }

    private String getFormattedIssueDate(java.time.LocalDateTime issuedAt) {
        java.time.LocalDateTime dateToUse = issuedAt;
        if (dateToUse == null) {
            dateToUse = java.time.LocalDateTime.now();
        }
        return dateToUse.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private String circleBeziers(double cx, double cy, double r) {
        double k = r * 0.5522847498;
        return String.format(java.util.Locale.US,
                "%.2f %.2f m\n" +
                "%.2f %.2f %.2f %.2f %.2f %.2f c\n" +
                "%.2f %.2f %.2f %.2f %.2f %.2f c\n" +
                "%.2f %.2f %.2f %.2f %.2f %.2f c\n" +
                "%.2f %.2f %.2f %.2f %.2f %.2f c\nS\n",
                cx + r, cy,
                cx + r, cy + k, cx + k, cy + r, cx, cy + r,
                cx - k, cy + r, cx - r, cy + k, cx - r, cy,
                cx - r, cy - k, cx - k, cy - r, cx, cy - r,
                cx + k, cy - r, cx + r, cy - k, cx + r, cy
        );
    }

    private List<String> wrapText(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() + (current.length() > 0 ? 1 : 0) + word.length() > maxChars) {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current = new StringBuilder();
                }
            }
            if (current.length() > 0) {
                current.append(" ");
            }
            current.append(word);
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private void drawText(StringBuilder content, String text, String fontRef, double fontSize,
                          double r, double g, double b, double x, double y) {
        content.append("BT\n");
        content.append(fontRef).append(" ").append(fontSize).append(" Tf\n");
        content.append(String.format(java.util.Locale.US, "%.3f %.3f %.3f rg\n", r, g, b));
        content.append(String.format(java.util.Locale.US, "%.1f %.1f Td\n", x, y));
        content.append("(").append(escapePdf(text)).append(") Tj\n");
        content.append("ET\n");
    }

    private void drawCenteredText(StringBuilder content, String text, String fontRef, double fontSize,
                                  double r, double g, double b, double centerX, double y) {
        double width = estimateTextWidth(text, fontRef, fontSize);
        double startX = centerX - (width / 2.0);
        drawText(content, text, fontRef, fontSize, r, g, b, startX, y);
    }

    private double estimateTextWidth(String text, String fontRef, double fontSize) {
        if (text == null || text.isEmpty()) return 0;
        double avgCharWidth = 0.52;
        if (fontRef.equals("/F1")) {
            avgCharWidth = 0.58;
        }
        return text.length() * fontSize * avgCharWidth;
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
