/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.config.jmx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Defines MBean which manage configuration. There should be only one instance of this object in the application and it
 * should be accessible even outside of the request cycle.
 *
 * @author Alex Objelean
 */
public final class WroConfiguration
  implements WroConfigurationMBean {
  private static final Logger LOG = LoggerFactory.getLogger(WroConfiguration.class);
  /**
   * Default encoding to use.
   */
  private static final String DEFAULT_ENCODING = "UTF-8";
  /**
	 * How often to run a thread responsible for refreshing the cache.
	 */
  private long cacheUpdatePeriod;
  /**
   * How often to run a thread responsible for refreshing the model.
   */
  private long modelUpdatePeriod;
  /**
   * Gzip enable flag.
   */
  private boolean gzipEnabled = true;
  /**
   * If true, we are running in DEVELOPMENT mode. By default this value is true.
   */
  private boolean debug = true;
  /**
   * If true, missing resources are ignored. By default this value is true.
   */
  private boolean ignoreMissingResources = true;
  /**
   * Flag which will force no caching of the processed content only in DEVELOPMENT mode. In DEPLOYMENT mode changing
   * this flag will have no effect. By default this value is false.
   */
  private boolean disableCache = false;
  /**
   * Allow to turn jmx on or off. By default thsi value is true.
   */
  private boolean jmxEnabled = true;
  /**
   * Encoding to use when reading resources.
   */
  private String encoding = DEFAULT_ENCODING;
  /**
   * Listeners for the change of cache & model period properties.
   */
  private final transient List<PropertyChangeListener> cacheUpdatePeriodListeners = new ArrayList<PropertyChangeListener>(1);
  private final transient List<PropertyChangeListener> modelUpdatePeriodListeners = new ArrayList<PropertyChangeListener>(1);

  /**
   * @return the name of the object used to register the MBean.
   */
  public static String getObjectName() {
    return WroConfiguration.class.getPackage().getName() + ".jmx:type=" + WroConfiguration.class.getSimpleName();
  }

  /**
   * {@inheritDoc}
   */
  public long getCacheUpdatePeriod() {
    return this.cacheUpdatePeriod;
  }


  /**
   * {@inheritDoc}
   */
  public long getModelUpdatePeriod() {
    return modelUpdatePeriod;
  }


  /**
   * {@inheritDoc}
   */
  public void setCacheUpdatePeriod(final long period) {
		if (period != cacheUpdatePeriod) {
			reloadCacheWithNewValue(period);
		}
  	this.cacheUpdatePeriod = period;
  }


  /**
   * {@inheritDoc}
   */
  public void setModelUpdatePeriod(final long period) {
		if (period != modelUpdatePeriod) {
			reloadModelWithNewValue(period);
		}
    this.modelUpdatePeriod = period;

  }

  /**
   * {@inheritDoc}
   */
  public boolean isGzipEnabled() {
  	return gzipEnabled;
  }

  /**
   * {@inheritDoc}
   */
  public void setGzipEnabled(final boolean enable) {
  	gzipEnabled = enable;
  }

  /**
   * {@inheritDoc}
   */
  public void reloadCache() {
    reloadCacheWithNewValue(null);
  }


	/**
	 * Notify all listeners about cachePeriod property changed. If passed newValue is null, the oldValue is taken as new
	 * value. This is the case when the reloadCache is invoked.
	 *
	 * @param newValue value to set.
	 */
	private void reloadCacheWithNewValue(final Long newValue) {
		final long newValueAsPrimitive = newValue == null ? getModelUpdatePeriod() : newValue;
    LOG.debug("invoking " + cacheUpdatePeriodListeners.size() + " listeners");
		for (final PropertyChangeListener listener : cacheUpdatePeriodListeners) {
  		final PropertyChangeEvent event = new PropertyChangeEvent(this, "cache", getCacheUpdatePeriod(), newValueAsPrimitive);
			listener.propertyChange(event);
		}
	}

  /**
   * {@inheritDoc}
   */
  public void reloadModel() {
    LOG.debug("reloadModel");
  	reloadModelWithNewValue(null);
  }


	/**
	 * Notify all listeners about cachePeriod property changed. If passed newValue is null, the oldValue is taken as new
	 * value. This is the case when the reloadModel is invoked.
	 *
	 * @param newValue value to set.
	 */
	private void reloadModelWithNewValue(final Long newValue) {
		final long newValueAsPrimitive = newValue == null ? getModelUpdatePeriod() : newValue;
  	for (final PropertyChangeListener listener : modelUpdatePeriodListeners) {
  		final PropertyChangeEvent event = new PropertyChangeEvent(this, "model", getModelUpdatePeriod(), newValueAsPrimitive);
			listener.propertyChange(event);
		}
	}

	/**
	 * Register a listener which is notified when the modelUpdate period value is changed. Registration is allowed only during
	 *
	 * @param listener to add.
	 */
	public void registerModelUpdatePeriodChangeListener(final PropertyChangeListener listener) {
		modelUpdatePeriodListeners.add(listener);
	}

	/**
	 * Register a listener which is notified when the modelUpdate period value is changed.
	 *
	 * @param listener to add.
	 */
	public void registerCacheUpdatePeriodChangeListener(final PropertyChangeListener listener) {
	  cacheUpdatePeriodListeners.add(listener);
	}

  /**
   * @return the debug
   */
  public boolean isDebug() {
    return this.debug;
  }

  /**
   * @param debug the debug to set
   */
  public void setDebug(final boolean debug) {
    //Don't think that we really need to reload the cache here
    this.debug = debug;
  }


  /**
   * @return the ignoreMissingResources
   */
  public boolean isIgnoreMissingResources() {
    return this.ignoreMissingResources;
  }

  /**
   * @param ignoreMissingResources the ignoreMissingResources to set
   */
  public void setIgnoreMissingResources(final boolean ignoreMissingResources) {
    this.ignoreMissingResources = ignoreMissingResources;
  }

  /**
   * @return the disableCache
   */
  public boolean isDisableCache() {
    //disable cache only if you are in DEVELOPMENT mode (aka debug)
    return this.disableCache && debug;
  }

  /**
   * @param disableCache the disableCache to set
   */
  public void setDisableCache(final boolean disableCache) {
    if (!debug) {
      LOG.warn("You cannot disable cache in DEPLOYMENT mode");
    }
    this.disableCache = disableCache;
  }

  /**
   * @return the jmxEnabled
   */
  public boolean isJmxEnabled() {
    return jmxEnabled;
  }

  /**
   * @param jmxEnabled the jmxEnabled to set
   */
  public void setJmxEnabled(final boolean jmxEnabled) {
    this.jmxEnabled = jmxEnabled;
  }

  /**
   * Perform the cleanup, clear the listeners.
   */
  public void destroy() {
    cacheUpdatePeriodListeners.clear();
    modelUpdatePeriodListeners.clear();
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return this.encoding == null ? DEFAULT_ENCODING : this.encoding;
  }

  /**
   * @param encoding the encoding to set
   */
  public void setEncoding(final String encoding) {
    this.encoding = encoding;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }
}
