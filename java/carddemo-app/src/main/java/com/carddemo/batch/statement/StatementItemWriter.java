package com.carddemo.batch.statement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

/**
 * {@code ItemWriter} for {@code creastmtJob}: renders each {@link AccountStatement} via
 * {@link StatementFormatter} and appends it to the two output files produced by legacy
 * {@code CBSTM03A} — the plain-text {@code STMTFILE} and the {@code HTMLFILE}.
 *
 * <p>The files are (re)created empty when the step opens and closed when it completes, so a
 * run yields the concatenation of every account's statement, exactly as the COBOL program
 * writes all statements to a single {@code STMTFILE}/{@code HTMLFILE}.</p>
 */
public class StatementItemWriter implements ItemStreamWriter<AccountStatement> {

    private final Path textFile;
    private final Path htmlFile;

    private BufferedWriter textWriter;
    private BufferedWriter htmlWriter;

    public StatementItemWriter(Path outputDir) {
        this.textFile = outputDir.resolve("statements.txt");
        this.htmlFile = outputDir.resolve("statements.html");
    }

    public Path getTextFile() {
        return textFile;
    }

    public Path getHtmlFile() {
        return htmlFile;
    }

    @Override
    public void open(ExecutionContext executionContext) {
        try {
            Files.createDirectories(textFile.getParent());
            textWriter = Files.newBufferedWriter(textFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            htmlWriter = Files.newBufferedWriter(htmlFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ItemStreamException("Unable to open statement output files", e);
        }
    }

    @Override
    public void write(Chunk<? extends AccountStatement> chunk) throws IOException {
        for (AccountStatement statement : chunk) {
            writeLines(textWriter, StatementFormatter.toTextLines(statement));
            writeLines(htmlWriter, StatementFormatter.toHtmlLines(statement));
        }
        textWriter.flush();
        htmlWriter.flush();
    }

    private static void writeLines(BufferedWriter writer, List<String> lines) throws IOException {
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        // No restart state to persist.
    }

    @Override
    public void close() {
        try {
            closeQuietly(textWriter);
            closeQuietly(htmlWriter);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to close statement output files", e);
        }
    }

    private static void closeQuietly(BufferedWriter writer) throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}
