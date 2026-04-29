package com.acme.reportai.parser;

import com.acme.reportai.model.ExecutionReport;
import java.nio.file.Path;

public interface ReportParser {
    boolean supports(Path source);
    ExecutionReport parse(Path source) throws Exception;
}
