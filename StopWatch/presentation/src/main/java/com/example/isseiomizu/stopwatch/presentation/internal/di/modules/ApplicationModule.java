/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.isseiomizu.stopwatch.presentation.internal.di.modules;

import android.content.Context;

import com.example.isseiomizu.stopwatch.domain.executor.PostExecutionThread;
import com.example.isseiomizu.stopwatch.presentation.UIThread;
import com.example.isseiomizu.stopwatch.presentation.StopWatchApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides objects which will live during the application lifecycle.
 */
@Module
public class ApplicationModule {
  private final StopWatchApplication application;

  public ApplicationModule(StopWatchApplication application) {
    this.application = application;
  }

  @Provides @Singleton
  Context provideApplicationContext() {
    return this.application;
  }

//  @Provides @Singleton
//  ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
//    return jobExecutor;
//  }

  @Provides @Singleton
  PostExecutionThread providePostExecutionThread(UIThread uiThread) {
    return uiThread;
  }

//  @Provides @Singleton UserCache provideUserCache(UserCacheImpl userCache) {
//    return userCache;
//  }
//
//  @Provides @Singleton UserRepository provideUserRepository(UserDataRepository userDataRepository) {
//    return userDataRepository;
//  }
}
