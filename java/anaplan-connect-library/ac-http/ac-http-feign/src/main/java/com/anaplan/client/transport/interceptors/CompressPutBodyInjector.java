package com.anaplan.client.transport.interceptors;

import com.anaplan.client.exceptions.BadFileChunkCompressionError;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Compresses the file-chunk PUT request if using X-Gzip compression, otherwise passes on the raw
 * value.
 */
public class CompressPutBodyInjector implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {

    // only for PUT operations (file chunk uploads), set the right mime-type (x-gzip or octet-stream)
    if (requestTemplate.request().httpMethod().name().equals("PUT")) {
      Collection<String> contentTypes = requestTemplate.headers().get("Content-Type");
      if (contentTypes != null &&
          ("application/x-gzip".equals(contentTypes.toArray()[0]) || "x-gzip"
              .equals(contentTypes.toArray()[0]))) {
        Charset charset = Optional.ofNullable(requestTemplate.charset()).orElse(StandardCharsets.UTF_8);
        requestTemplate.body(compress(requestTemplate.body()), charset);
      }
    }
  }

  private byte[] compress(byte[] source) {
    int head = (source[0] & 0xff) | ((source[1] << 8) & 0xff00);
    ByteArrayOutputStream sink = new ByteArrayOutputStream();
    //check if matches standard gzip magic number
    if (GZIPInputStream.GZIP_MAGIC != head) {
      try {
        GZIPOutputStream gzos = new GZIPOutputStream(sink);
        gzos.write(source, 0, source.length);
        gzos.close();
        return sink.toByteArray();
      } catch (IOException e) {
        throw new BadFileChunkCompressionError(e);
      }
    } else {
      return source;
    }
  }
}
