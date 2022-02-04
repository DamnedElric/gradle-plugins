package io.freefair.gradle.plugins.okhttp.tasks;

import lombok.Getter;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

import java.io.IOException;

/**
 * @author Lars Grefer
 */
@Getter
public class DownloadFile extends HttpGet {

    @OutputFile
    private final RegularFileProperty outputFile = getProject().getObjects().fileProperty();

    @Override
    public void handleResponse(Response response) throws IOException {
        super.handleResponse(response);

        try (BufferedSink sink = Okio.buffer(Okio.sink(outputFile.getAsFile().get()))) {
            sink.writeAll(response.body().source());
        }
    }
}
