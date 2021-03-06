/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.manager;

import java.beans.PropertyChangeListener;


/**
 * The {@link WroManagerFactory} classes which implement this interface allows registration of a callback to be invoked
 * when the cache is changed.
 *
 * @author Alex Objelean
 */
public interface CacheChangeCallbackAware {
  /**
   * @param callback to invoke when the cache is changed.
   */
  void registerCallback(PropertyChangeListener callback);
}
