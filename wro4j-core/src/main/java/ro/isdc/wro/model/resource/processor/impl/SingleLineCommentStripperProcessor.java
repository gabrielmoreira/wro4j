/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroUtil;


/**
 * SingleLineCommentStripperProcessor can be both: preProcessor & postProcessor. Remove single line comments from
 * processed resource. This processor exist only for the sake of proof of concept.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class SingleLineCommentStripperProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * Pattern containing a regex matching singleline comments & preceding empty spaces & tabs.
   */
  public static Pattern PATTERN = Pattern.compile("[\\t ]*//.*?$", Pattern.MULTILINE);


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String content = IOUtils.toString(reader);
      String result = PATTERN.matcher(content).replaceAll("");
      result = WroUtil.EMTPY_LINE_PATTERN.matcher(result).replaceAll("");
      writer.write(result);
    } finally {
      reader.close();
      writer.close();
    }
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    // resource Uri doesn't matter.
    process(reader, writer);
  }
}
