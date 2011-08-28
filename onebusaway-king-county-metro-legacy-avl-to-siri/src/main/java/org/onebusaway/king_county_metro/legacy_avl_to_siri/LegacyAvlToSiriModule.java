package org.onebusaway.king_county_metro.legacy_avl_to_siri;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.AbstractModule;

public class LegacyAvlToSiriModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(LegacyAvlToSiriTask.class);
    bind(ExecutorService.class).toInstance(Executors.newSingleThreadExecutor());
  }
}
