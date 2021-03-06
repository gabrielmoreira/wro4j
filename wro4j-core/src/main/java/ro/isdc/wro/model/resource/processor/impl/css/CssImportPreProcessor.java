/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.PreProcessorExecutor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.StringUtils;


/**
 * CssImport Processor responsible for handling css <code>@import</code> statement. It is implemented as both:
 * preProcessor & postProcessor. It is necessary because preProcessor is responsible for updating model with found
 * imported resources, while post processor removes import occurrences.
 *
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.CSS)
public class CssImportPreProcessor
  implements ResourcePreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssImportPreProcessor.class);
  /**
   * Contains a {@link UriLocatorFactory} reference injected externally.
   */
  @Inject
  private UriLocatorFactory uriLocatorFactory;
  @Inject
  private PreProcessorExecutor preProcessorExecutor;
  /**
   * List of processed resources, useful for detecting deep recursion.
   */
  private final List<Resource> processed = new ArrayList<Resource>();
  /** The url pattern */
  private static final Pattern PATTERN = Pattern.compile("@import\\s*(?:url\\()?[\"']?([^\"')]+)[\"')]?\\)?;?", Pattern.CASE_INSENSITIVE);

  /**
   * {@inheritDoc}
   */
  public synchronized void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    validate();
    try {
      final String result = parseCss(resource, reader);
      writer.write(result);
      processed.clear();
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * Checks if required fields were injected.
   */
  private void validate() {
    if (uriLocatorFactory == null) {
      throw new IllegalStateException("No UriLocator was injected");
    }
    if (preProcessorExecutor == null) {
      throw new IllegalStateException("No preProcessorExecutor was injected");
    }
  }


  /**
   * @param resource {@link Resource} to process.
   * @param reader Reader for processed resource.
   * @return css content with all imports processed.
   */
  private String parseCss(final Resource resource, final Reader reader)
    throws IOException {
    if (processed.contains(resource)) {
      LOG.warn("Recursive import detected: " + resource);
      return "";
    }
    processed.add(resource);
    final StringBuffer sb = new StringBuffer();
    final List<Resource> importsCollector = getImportedResources(resource);
    // for now, minimize always
    // TODO: find a way to get minimize property dynamically.
    //groupExtractor.isMinimized(Context.get().getRequest())
    sb.append(preProcessorExecutor.processAndMerge(importsCollector, true));
    if (!importsCollector.isEmpty()) {
      LOG.debug("Imported resources found : " + importsCollector.size());
    }
    sb.append(IOUtils.toString(reader));
    LOG.debug("importsCollector: " + importsCollector);
    return removeImportStatements(sb.toString());
  }


  /**
   * Removes all @import statements for css.
   */
  private String removeImportStatements(final String content) {
    final Matcher m = PATTERN.matcher(content);
    final StringBuffer sb = new StringBuffer();
    while (m.find()) {
      // add and check if already exist
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * Find a set of imported resources inside a given resource.
   */
  private List<Resource> getImportedResources(final Resource resource)
    throws IOException {
    // it should be sorted
    final List<Resource> imports = new ArrayList<Resource>();
    final String css = IOUtils.toString(uriLocatorFactory.locate(resource.getUri()),
      Context.get().getConfig().getEncoding());
    final Matcher m = PATTERN.matcher(css);
    while (m.find()) {
      final Resource importedResource = buildImportedResource(resource, m.group(1));
      // check if already exist
      if (imports.contains(importedResource)) {
        LOG.warn("Duplicate imported resource: " + importedResource);
      } else {
        imports.add(importedResource);
      }
    }
    return imports;
  }


  /**
   * Build a {@link Resource} object from a found importedResource inside a given resource.
   */
  private Resource buildImportedResource(final Resource resource, final String importUrl) {
    final String absoluteUrl = computeAbsoluteUrl(resource, importUrl);
    return Resource.create(absoluteUrl, ResourceType.CSS);
  }


  /**
   * Computes absolute url of the imported resource.
   *
   * @param relativeResource {@link Resource} where the import statement is found.
   * @param importUrl found import url.
   * @return absolute url of the resource to import.
   */
  private String computeAbsoluteUrl(final Resource relativeResource, final String importUrl) {
    final String folder = FilenameUtils.getFullPath(relativeResource.getUri());
    // remove '../' & normalize the path.
    final String absoluteImportUrl = StringUtils.normalizePath(folder + importUrl);
    return absoluteImportUrl;
  }
}
