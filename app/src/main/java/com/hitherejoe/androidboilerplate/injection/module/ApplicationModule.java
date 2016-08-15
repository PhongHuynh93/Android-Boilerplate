package com.hitherejoe.androidboilerplate.injection.module;

import android.app.Application;
import android.content.Context;

import com.hitherejoe.androidboilerplate.data.remote.AndroidBoilerplateService;
import com.hitherejoe.androidboilerplate.injection.ApplicationContext;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provide application-level dependencies. Mainly singleton object that can be injected from
 * anywhere in the app.
 *
 * // TODO: 8/15/16 3 declare module
 */
@Module
public class ApplicationModule {
    protected final Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return mApplication;
    }

    // declare retrofit service
    @Provides
    @Singleton
    AndroidBoilerplateService provideAndroidBoilerplateService() {
        return AndroidBoilerplateService.Factory.makeAndroidBoilerplateService(mApplication);
    }

}