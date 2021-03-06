/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * A factory using experimental features, like {@link CssDataUriPreProcessor} which is not fully supported by all
 * browsers.
 *
 * @author Alex Objelean
 * @created May 9, 2010
 * @deprecated (Will be removed in 1.4.0) Use {@link ConfigurableWroManagerFactory} instead with <code>cssDataUri</code>
 *             preProcessor set.
 */
@Deprecated
public class ExperimentalWroManagerFactory extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.addPreProcessor(new CssDataUriPreProcessor());
    factory.addPreProcessor(new BomStripperPreProcessor());
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new SemicolonAppenderPreProcessor());
    factory.addPreProcessor(new JSMinProcessor());
    factory.addPreProcessor(new JawrCssMinifierProcessor());

    factory.addPostProcessor(new CssVariablesProcessor());

    return factory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactory factory = new SimpleUriLocatorFactory().addUriLocator(
        new ServletContextUriLocator()).addUriLocator(new ClasspathUriLocator()).addUriLocator(new UrlUriLocator());
    return factory;
  }
}
