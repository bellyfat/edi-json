package com.berryworks.edireader.json;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

public class EdiToJsonDriver implements Runnable {
    private final File ediFile;
    private final File jsonFile;
    private boolean summarize, annotate, format;

    public EdiToJsonDriver(File inEdiFile, File inJsonFile) {
        ediFile = inEdiFile;
        jsonFile = inJsonFile;
        // Set defaults for options
        summarize = false;
        annotate = false;
        format = true;
    }

    @Override
    public void run() {
        final EdiToJson ediToJson = new EdiToJson();
        ediToJson.setFormatting(format);
        ediToJson.setAnnotated(annotate);
        try (Reader reader = new FileReader(ediFile); Writer writer = new FileWriter(jsonFile)) {
            ediToJson.asJson(reader, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            badArgs();
            return;
        }

        // The first two args are the input and output filenames.
        File ediFile = new File(args[0]);
        if (!ediFile.exists() || !ediFile.canRead()) {
            throw new RuntimeException("Cannot find or read EDI input file: " + ediFile.getAbsolutePath());
        }
        File jsonFile = new File(args[1]);
        if (!jsonFile.canWrite()) {
            throw new RuntimeException("Cannot write to Json output file: " + jsonFile.getAbsolutePath());
        }

        final EdiToJsonDriver driver = new EdiToJsonDriver(ediFile, jsonFile);

        // Any remaining args beginning with "--" are treated as options
        for (int i = 2; i < args.length; i++) {
            final String[] split = args[i].split("=");
            if (split.length != 2) continue;
            String optionName = split[0];
            String yesOrNo = split[1];
            switch (optionName) {
                case "--summarize":
                    driver.setSummarize("yes".equalsIgnoreCase(yesOrNo));
                    break;
                case "--annotate":
                    driver.setAnnotate("yes".equalsIgnoreCase(yesOrNo));
                    break;
                case "--format":
                    driver.setFormat("yes".equalsIgnoreCase(yesOrNo));
                    break;
            }
        }

        driver.run();
    }

    private static void badArgs() {
        System.err.println("Invalid command line arguments");
        logUsage();
    }

    private static void logUsage() {
        log();
        log("usage:  java -jar <jar-file-name>  ediInputFile  jsonOutputFile  <options>");
        log();
        log("options");
        log("  ", "--summarize={yes|no}", ":", "if yes, omit segment-level detail (default is no)");
        log("  ", "--annotate={yes|no}", ":", "if yes, include descriptive \"annotations\" (default is no)");
        log("  ", "--format={yes|no}", ":", "if yes, format JSON output (default is yes)");
        log();
    }

    private static void log(Object... items) {
        StringBuilder sb = new StringBuilder();
        for (Object item : items) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(item.toString());
        }
        System.out.println(sb.toString());
    }

    private void setSummarize(boolean summarize) {
        this.summarize = summarize;
    }

    private void setAnnotate(boolean annotate) {
        this.annotate = annotate;
    }

    private void setFormat(boolean format) {
        this.format = format;
    }
}

