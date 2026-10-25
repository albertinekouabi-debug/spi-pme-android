package com.spipme.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.spipme.app.core.network.AuthInterceptor;
import com.spipme.app.core.network.NetworkModule_FournirAdminApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirAlerteApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirAnalyticsApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirAuditApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirAuthApiBrutFactory;
import com.spipme.app.core.network.NetworkModule_FournirAuthApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirConformiteApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirEntiteApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirFactureApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirImportApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirJsonFactory;
import com.spipme.app.core.network.NetworkModule_FournirLoggingInterceptorFactory;
import com.spipme.app.core.network.NetworkModule_FournirMoiApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirOkHttpClientAuthentifieFactory;
import com.spipme.app.core.network.NetworkModule_FournirOkHttpClientBrutFactory;
import com.spipme.app.core.network.NetworkModule_FournirRessourceApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirRetrofitAuthentifieFactory;
import com.spipme.app.core.network.NetworkModule_FournirRetrofitBrutFactory;
import com.spipme.app.core.network.NetworkModule_FournirSecteurApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirSuggestionApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirTacheApiFactory;
import com.spipme.app.core.network.NetworkModule_FournirTransactionApiFactory;
import com.spipme.app.core.network.TokenAuthenticator;
import com.spipme.app.core.notifications.NotificationBadgeManager;
import com.spipme.app.core.notifications.NotificationBadgeViewModel;
import com.spipme.app.core.notifications.NotificationBadgeViewModel_HiltModules;
import com.spipme.app.core.security.SessionManager;
import com.spipme.app.core.security.TokenManager;
import com.spipme.app.core.sync.SyncEngine;
import com.spipme.app.core.sync.android.ConnectivityMonitor;
import com.spipme.app.core.sync.android.EcritureHorsLigne;
import com.spipme.app.core.sync.android.PasserelleOkHttp;
import com.spipme.app.core.sync.android.RoomCorrespondances;
import com.spipme.app.core.sync.android.RoomFileOperations;
import com.spipme.app.core.sync.android.SyncActivite;
import com.spipme.app.core.sync.android.SyncModule_FournirMoteurFactory;
import com.spipme.app.core.sync.android.SyncScheduler;
import com.spipme.app.core.sync.android.SynchronisationRepository;
import com.spipme.app.data.local.AppDatabase;
import com.spipme.app.data.local.CacheLecture;
import com.spipme.app.data.local.dao.CacheDao;
import com.spipme.app.data.local.dao.CorrespondanceDao;
import com.spipme.app.data.local.dao.OperationDao;
import com.spipme.app.data.local.dao.UtilisateurLocalDao;
import com.spipme.app.data.remote.api.AdminApi;
import com.spipme.app.data.remote.api.AlerteApi;
import com.spipme.app.data.remote.api.AnalyticsApi;
import com.spipme.app.data.remote.api.AuditApi;
import com.spipme.app.data.remote.api.AuthApi;
import com.spipme.app.data.remote.api.ConformiteApi;
import com.spipme.app.data.remote.api.EntiteApi;
import com.spipme.app.data.remote.api.FactureApi;
import com.spipme.app.data.remote.api.ImportApi;
import com.spipme.app.data.remote.api.MoiApi;
import com.spipme.app.data.remote.api.RessourceApi;
import com.spipme.app.data.remote.api.SecteurApi;
import com.spipme.app.data.remote.api.SuggestionApi;
import com.spipme.app.data.remote.api.TacheApi;
import com.spipme.app.data.remote.api.TransactionApi;
import com.spipme.app.data.repository.AdminRepositoryImpl;
import com.spipme.app.data.repository.AlerteRepositoryImpl;
import com.spipme.app.data.repository.AnalyticsRepositoryImpl;
import com.spipme.app.data.repository.AuditRepositoryImpl;
import com.spipme.app.data.repository.AuthRepositoryImpl;
import com.spipme.app.data.repository.ConformiteRepositoryImpl;
import com.spipme.app.data.repository.EntiteRepositoryImpl;
import com.spipme.app.data.repository.FactureRepositoryImpl;
import com.spipme.app.data.repository.ImportRepositoryImpl;
import com.spipme.app.data.repository.MoiRepositoryImpl;
import com.spipme.app.data.repository.RessourceRepositoryImpl;
import com.spipme.app.data.repository.SecteurRepositoryImpl;
import com.spipme.app.data.repository.SuggestionRepositoryImpl;
import com.spipme.app.data.repository.TacheRepositoryImpl;
import com.spipme.app.data.repository.TransactionRepositoryImpl;
import com.spipme.app.di.DatabaseModule_FournirAppDatabaseFactory;
import com.spipme.app.di.DatabaseModule_FournirCacheDaoFactory;
import com.spipme.app.di.DatabaseModule_FournirCorrespondanceDaoFactory;
import com.spipme.app.di.DatabaseModule_FournirOperationDaoFactory;
import com.spipme.app.di.DatabaseModule_FournirUtilisateurLocalDaoFactory;
import com.spipme.app.ui.admin.AdminViewModel;
import com.spipme.app.ui.admin.AdminViewModel_HiltModules;
import com.spipme.app.ui.alerts.AlertsViewModel;
import com.spipme.app.ui.alerts.AlertsViewModel_HiltModules;
import com.spipme.app.ui.audit.AuditViewModel;
import com.spipme.app.ui.audit.AuditViewModel_HiltModules;
import com.spipme.app.ui.auth.login.LoginViewModel;
import com.spipme.app.ui.auth.login.LoginViewModel_HiltModules;
import com.spipme.app.ui.auth.motdepasseoublie.MotDePasseOublieViewModel;
import com.spipme.app.ui.auth.motdepasseoublie.MotDePasseOublieViewModel_HiltModules;
import com.spipme.app.ui.auth.offline.OfflineLoginViewModel;
import com.spipme.app.ui.auth.offline.OfflineLoginViewModel_HiltModules;
import com.spipme.app.ui.auth.register.RegisterViewModel;
import com.spipme.app.ui.auth.register.RegisterViewModel_HiltModules;
import com.spipme.app.ui.compliance.ComplianceViewModel;
import com.spipme.app.ui.compliance.ComplianceViewModel_HiltModules;
import com.spipme.app.ui.components.SecteurSelectorViewModel;
import com.spipme.app.ui.components.SecteurSelectorViewModel_HiltModules;
import com.spipme.app.ui.dashboard.DashboardViewModel;
import com.spipme.app.ui.dashboard.DashboardViewModel_HiltModules;
import com.spipme.app.ui.home.HomeViewModel;
import com.spipme.app.ui.home.HomeViewModel_HiltModules;
import com.spipme.app.ui.imports.ImportsViewModel;
import com.spipme.app.ui.imports.ImportsViewModel_HiltModules;
import com.spipme.app.ui.intelligence.IntelligenceViewModel;
import com.spipme.app.ui.intelligence.IntelligenceViewModel_HiltModules;
import com.spipme.app.ui.invoices.InvoicesViewModel;
import com.spipme.app.ui.invoices.InvoicesViewModel_HiltModules;
import com.spipme.app.ui.pilotage.PilotageViewModel;
import com.spipme.app.ui.pilotage.PilotageViewModel_HiltModules;
import com.spipme.app.ui.profile.ProfileViewModel;
import com.spipme.app.ui.profile.ProfileViewModel_HiltModules;
import com.spipme.app.ui.registry.RegistryViewModel;
import com.spipme.app.ui.registry.RegistryViewModel_HiltModules;
import com.spipme.app.ui.registry.creation.CreerEntiteViewModel;
import com.spipme.app.ui.registry.creation.CreerEntiteViewModel_HiltModules;
import com.spipme.app.ui.resources.ResourcesViewModel;
import com.spipme.app.ui.resources.ResourcesViewModel_HiltModules;
import com.spipme.app.ui.resources.creation.CreerRessourceViewModel;
import com.spipme.app.ui.resources.creation.CreerRessourceViewModel_HiltModules;
import com.spipme.app.ui.sync.SyncStatusViewModel;
import com.spipme.app.ui.sync.SyncStatusViewModel_HiltModules;
import com.spipme.app.ui.tasks.TasksViewModel;
import com.spipme.app.ui.tasks.TasksViewModel_HiltModules;
import com.spipme.app.ui.tasks.creation.CreerTacheViewModel;
import com.spipme.app.ui.tasks.creation.CreerTacheViewModel_HiltModules;
import com.spipme.app.ui.treasury.TreasuryViewModel;
import com.spipme.app.ui.treasury.TreasuryViewModel_HiltModules;
import com.spipme.app.ui.treasury.creation.CreerTransactionViewModel;
import com.spipme.app.ui.treasury.creation.CreerTransactionViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class DaggerSpiPmeApplication_HiltComponents_SingletonC {
  private DaggerSpiPmeApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public SpiPmeApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements SpiPmeApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public SpiPmeApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements SpiPmeApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public SpiPmeApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements SpiPmeApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public SpiPmeApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements SpiPmeApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SpiPmeApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements SpiPmeApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SpiPmeApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements SpiPmeApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public SpiPmeApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements SpiPmeApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public SpiPmeApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends SpiPmeApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends SpiPmeApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends SpiPmeApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends SpiPmeApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(27).put(LazyClassKeyProvider.com_spipme_app_ui_admin_AdminViewModel, AdminViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_alerts_AlertsViewModel, AlertsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_AppViewModel, AppViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_audit_AuditViewModel, AuditViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_compliance_ComplianceViewModel, ComplianceViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_registry_creation_CreerEntiteViewModel, CreerEntiteViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_resources_creation_CreerRessourceViewModel, CreerRessourceViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_tasks_creation_CreerTacheViewModel, CreerTacheViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_treasury_creation_CreerTransactionViewModel, CreerTransactionViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_dashboard_DashboardViewModel, DashboardViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_imports_ImportsViewModel, ImportsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_intelligence_IntelligenceViewModel, IntelligenceViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_invoices_InvoicesViewModel, InvoicesViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_auth_login_LoginViewModel, LoginViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_auth_motdepasseoublie_MotDePasseOublieViewModel, MotDePasseOublieViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_core_notifications_NotificationBadgeViewModel, NotificationBadgeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_auth_offline_OfflineLoginViewModel, OfflineLoginViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_pilotage_PilotageViewModel, PilotageViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_profile_ProfileViewModel, ProfileViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_auth_register_RegisterViewModel, RegisterViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_registry_RegistryViewModel, RegistryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_resources_ResourcesViewModel, ResourcesViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_components_SecteurSelectorViewModel, SecteurSelectorViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_sync_SyncStatusViewModel, SyncStatusViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_tasks_TasksViewModel, TasksViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_spipme_app_ui_treasury_TreasuryViewModel, TreasuryViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_spipme_app_core_notifications_NotificationBadgeViewModel = "com.spipme.app.core.notifications.NotificationBadgeViewModel";

      static String com_spipme_app_ui_components_SecteurSelectorViewModel = "com.spipme.app.ui.components.SecteurSelectorViewModel";

      static String com_spipme_app_ui_auth_register_RegisterViewModel = "com.spipme.app.ui.auth.register.RegisterViewModel";

      static String com_spipme_app_ui_intelligence_IntelligenceViewModel = "com.spipme.app.ui.intelligence.IntelligenceViewModel";

      static String com_spipme_app_ui_sync_SyncStatusViewModel = "com.spipme.app.ui.sync.SyncStatusViewModel";

      static String com_spipme_app_ui_alerts_AlertsViewModel = "com.spipme.app.ui.alerts.AlertsViewModel";

      static String com_spipme_app_ui_audit_AuditViewModel = "com.spipme.app.ui.audit.AuditViewModel";

      static String com_spipme_app_ui_dashboard_DashboardViewModel = "com.spipme.app.ui.dashboard.DashboardViewModel";

      static String com_spipme_app_ui_invoices_InvoicesViewModel = "com.spipme.app.ui.invoices.InvoicesViewModel";

      static String com_spipme_app_ui_admin_AdminViewModel = "com.spipme.app.ui.admin.AdminViewModel";

      static String com_spipme_app_ui_compliance_ComplianceViewModel = "com.spipme.app.ui.compliance.ComplianceViewModel";

      static String com_spipme_app_ui_pilotage_PilotageViewModel = "com.spipme.app.ui.pilotage.PilotageViewModel";

      static String com_spipme_app_ui_auth_login_LoginViewModel = "com.spipme.app.ui.auth.login.LoginViewModel";

      static String com_spipme_app_ui_tasks_creation_CreerTacheViewModel = "com.spipme.app.ui.tasks.creation.CreerTacheViewModel";

      static String com_spipme_app_ui_treasury_creation_CreerTransactionViewModel = "com.spipme.app.ui.treasury.creation.CreerTransactionViewModel";

      static String com_spipme_app_ui_profile_ProfileViewModel = "com.spipme.app.ui.profile.ProfileViewModel";

      static String com_spipme_app_ui_tasks_TasksViewModel = "com.spipme.app.ui.tasks.TasksViewModel";

      static String com_spipme_app_ui_auth_offline_OfflineLoginViewModel = "com.spipme.app.ui.auth.offline.OfflineLoginViewModel";

      static String com_spipme_app_ui_registry_RegistryViewModel = "com.spipme.app.ui.registry.RegistryViewModel";

      static String com_spipme_app_ui_auth_motdepasseoublie_MotDePasseOublieViewModel = "com.spipme.app.ui.auth.motdepasseoublie.MotDePasseOublieViewModel";

      static String com_spipme_app_ui_resources_ResourcesViewModel = "com.spipme.app.ui.resources.ResourcesViewModel";

      static String com_spipme_app_ui_registry_creation_CreerEntiteViewModel = "com.spipme.app.ui.registry.creation.CreerEntiteViewModel";

      static String com_spipme_app_ui_resources_creation_CreerRessourceViewModel = "com.spipme.app.ui.resources.creation.CreerRessourceViewModel";

      static String com_spipme_app_AppViewModel = "com.spipme.app.AppViewModel";

      static String com_spipme_app_ui_home_HomeViewModel = "com.spipme.app.ui.home.HomeViewModel";

      static String com_spipme_app_ui_treasury_TreasuryViewModel = "com.spipme.app.ui.treasury.TreasuryViewModel";

      static String com_spipme_app_ui_imports_ImportsViewModel = "com.spipme.app.ui.imports.ImportsViewModel";

      @KeepFieldType
      NotificationBadgeViewModel com_spipme_app_core_notifications_NotificationBadgeViewModel2;

      @KeepFieldType
      SecteurSelectorViewModel com_spipme_app_ui_components_SecteurSelectorViewModel2;

      @KeepFieldType
      RegisterViewModel com_spipme_app_ui_auth_register_RegisterViewModel2;

      @KeepFieldType
      IntelligenceViewModel com_spipme_app_ui_intelligence_IntelligenceViewModel2;

      @KeepFieldType
      SyncStatusViewModel com_spipme_app_ui_sync_SyncStatusViewModel2;

      @KeepFieldType
      AlertsViewModel com_spipme_app_ui_alerts_AlertsViewModel2;

      @KeepFieldType
      AuditViewModel com_spipme_app_ui_audit_AuditViewModel2;

      @KeepFieldType
      DashboardViewModel com_spipme_app_ui_dashboard_DashboardViewModel2;

      @KeepFieldType
      InvoicesViewModel com_spipme_app_ui_invoices_InvoicesViewModel2;

      @KeepFieldType
      AdminViewModel com_spipme_app_ui_admin_AdminViewModel2;

      @KeepFieldType
      ComplianceViewModel com_spipme_app_ui_compliance_ComplianceViewModel2;

      @KeepFieldType
      PilotageViewModel com_spipme_app_ui_pilotage_PilotageViewModel2;

      @KeepFieldType
      LoginViewModel com_spipme_app_ui_auth_login_LoginViewModel2;

      @KeepFieldType
      CreerTacheViewModel com_spipme_app_ui_tasks_creation_CreerTacheViewModel2;

      @KeepFieldType
      CreerTransactionViewModel com_spipme_app_ui_treasury_creation_CreerTransactionViewModel2;

      @KeepFieldType
      ProfileViewModel com_spipme_app_ui_profile_ProfileViewModel2;

      @KeepFieldType
      TasksViewModel com_spipme_app_ui_tasks_TasksViewModel2;

      @KeepFieldType
      OfflineLoginViewModel com_spipme_app_ui_auth_offline_OfflineLoginViewModel2;

      @KeepFieldType
      RegistryViewModel com_spipme_app_ui_registry_RegistryViewModel2;

      @KeepFieldType
      MotDePasseOublieViewModel com_spipme_app_ui_auth_motdepasseoublie_MotDePasseOublieViewModel2;

      @KeepFieldType
      ResourcesViewModel com_spipme_app_ui_resources_ResourcesViewModel2;

      @KeepFieldType
      CreerEntiteViewModel com_spipme_app_ui_registry_creation_CreerEntiteViewModel2;

      @KeepFieldType
      CreerRessourceViewModel com_spipme_app_ui_resources_creation_CreerRessourceViewModel2;

      @KeepFieldType
      AppViewModel com_spipme_app_AppViewModel2;

      @KeepFieldType
      HomeViewModel com_spipme_app_ui_home_HomeViewModel2;

      @KeepFieldType
      TreasuryViewModel com_spipme_app_ui_treasury_TreasuryViewModel2;

      @KeepFieldType
      ImportsViewModel com_spipme_app_ui_imports_ImportsViewModel2;
    }
  }

  private static final class ViewModelCImpl extends SpiPmeApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AdminViewModel> adminViewModelProvider;

    private Provider<AlertsViewModel> alertsViewModelProvider;

    private Provider<AppViewModel> appViewModelProvider;

    private Provider<AuditViewModel> auditViewModelProvider;

    private Provider<ComplianceViewModel> complianceViewModelProvider;

    private Provider<CreerEntiteViewModel> creerEntiteViewModelProvider;

    private Provider<CreerRessourceViewModel> creerRessourceViewModelProvider;

    private Provider<CreerTacheViewModel> creerTacheViewModelProvider;

    private Provider<CreerTransactionViewModel> creerTransactionViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<ImportsViewModel> importsViewModelProvider;

    private Provider<IntelligenceViewModel> intelligenceViewModelProvider;

    private Provider<InvoicesViewModel> invoicesViewModelProvider;

    private Provider<LoginViewModel> loginViewModelProvider;

    private Provider<MotDePasseOublieViewModel> motDePasseOublieViewModelProvider;

    private Provider<NotificationBadgeViewModel> notificationBadgeViewModelProvider;

    private Provider<OfflineLoginViewModel> offlineLoginViewModelProvider;

    private Provider<PilotageViewModel> pilotageViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<RegisterViewModel> registerViewModelProvider;

    private Provider<RegistryViewModel> registryViewModelProvider;

    private Provider<ResourcesViewModel> resourcesViewModelProvider;

    private Provider<SecteurSelectorViewModel> secteurSelectorViewModelProvider;

    private Provider<SyncStatusViewModel> syncStatusViewModelProvider;

    private Provider<TasksViewModel> tasksViewModelProvider;

    private Provider<TreasuryViewModel> treasuryViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);
      initialize2(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.adminViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.alertsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.appViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.auditViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.complianceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.creerEntiteViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.creerRessourceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.creerTacheViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.creerTransactionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.importsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.intelligenceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.invoicesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.motDePasseOublieViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
      this.notificationBadgeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 16);
      this.offlineLoginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 17);
      this.pilotageViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 18);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 19);
      this.registerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 20);
      this.registryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 21);
      this.resourcesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 22);
      this.secteurSelectorViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 23);
      this.syncStatusViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 24);
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.tasksViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 25);
      this.treasuryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 26);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(27).put(LazyClassKeyProvider.com_spipme_app_ui_admin_AdminViewModel, ((Provider) adminViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_alerts_AlertsViewModel, ((Provider) alertsViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_AppViewModel, ((Provider) appViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_audit_AuditViewModel, ((Provider) auditViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_compliance_ComplianceViewModel, ((Provider) complianceViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_registry_creation_CreerEntiteViewModel, ((Provider) creerEntiteViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_resources_creation_CreerRessourceViewModel, ((Provider) creerRessourceViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_tasks_creation_CreerTacheViewModel, ((Provider) creerTacheViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_treasury_creation_CreerTransactionViewModel, ((Provider) creerTransactionViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_dashboard_DashboardViewModel, ((Provider) dashboardViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_imports_ImportsViewModel, ((Provider) importsViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_intelligence_IntelligenceViewModel, ((Provider) intelligenceViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_invoices_InvoicesViewModel, ((Provider) invoicesViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_auth_login_LoginViewModel, ((Provider) loginViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_auth_motdepasseoublie_MotDePasseOublieViewModel, ((Provider) motDePasseOublieViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_core_notifications_NotificationBadgeViewModel, ((Provider) notificationBadgeViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_auth_offline_OfflineLoginViewModel, ((Provider) offlineLoginViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_pilotage_PilotageViewModel, ((Provider) pilotageViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_profile_ProfileViewModel, ((Provider) profileViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_auth_register_RegisterViewModel, ((Provider) registerViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_registry_RegistryViewModel, ((Provider) registryViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_resources_ResourcesViewModel, ((Provider) resourcesViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_components_SecteurSelectorViewModel, ((Provider) secteurSelectorViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_sync_SyncStatusViewModel, ((Provider) syncStatusViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_tasks_TasksViewModel, ((Provider) tasksViewModelProvider)).put(LazyClassKeyProvider.com_spipme_app_ui_treasury_TreasuryViewModel, ((Provider) treasuryViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_spipme_app_ui_treasury_TreasuryViewModel = "com.spipme.app.ui.treasury.TreasuryViewModel";

      static String com_spipme_app_ui_compliance_ComplianceViewModel = "com.spipme.app.ui.compliance.ComplianceViewModel";

      static String com_spipme_app_ui_alerts_AlertsViewModel = "com.spipme.app.ui.alerts.AlertsViewModel";

      static String com_spipme_app_ui_intelligence_IntelligenceViewModel = "com.spipme.app.ui.intelligence.IntelligenceViewModel";

      static String com_spipme_app_ui_dashboard_DashboardViewModel = "com.spipme.app.ui.dashboard.DashboardViewModel";

      static String com_spipme_app_ui_imports_ImportsViewModel = "com.spipme.app.ui.imports.ImportsViewModel";

      static String com_spipme_app_ui_tasks_TasksViewModel = "com.spipme.app.ui.tasks.TasksViewModel";

      static String com_spipme_app_ui_audit_AuditViewModel = "com.spipme.app.ui.audit.AuditViewModel";

      static String com_spipme_app_ui_resources_creation_CreerRessourceViewModel = "com.spipme.app.ui.resources.creation.CreerRessourceViewModel";

      static String com_spipme_app_ui_profile_ProfileViewModel = "com.spipme.app.ui.profile.ProfileViewModel";

      static String com_spipme_app_ui_resources_ResourcesViewModel = "com.spipme.app.ui.resources.ResourcesViewModel";

      static String com_spipme_app_core_notifications_NotificationBadgeViewModel = "com.spipme.app.core.notifications.NotificationBadgeViewModel";

      static String com_spipme_app_ui_auth_register_RegisterViewModel = "com.spipme.app.ui.auth.register.RegisterViewModel";

      static String com_spipme_app_ui_components_SecteurSelectorViewModel = "com.spipme.app.ui.components.SecteurSelectorViewModel";

      static String com_spipme_app_ui_admin_AdminViewModel = "com.spipme.app.ui.admin.AdminViewModel";

      static String com_spipme_app_ui_home_HomeViewModel = "com.spipme.app.ui.home.HomeViewModel";

      static String com_spipme_app_ui_auth_offline_OfflineLoginViewModel = "com.spipme.app.ui.auth.offline.OfflineLoginViewModel";

      static String com_spipme_app_AppViewModel = "com.spipme.app.AppViewModel";

      static String com_spipme_app_ui_auth_login_LoginViewModel = "com.spipme.app.ui.auth.login.LoginViewModel";

      static String com_spipme_app_ui_pilotage_PilotageViewModel = "com.spipme.app.ui.pilotage.PilotageViewModel";

      static String com_spipme_app_ui_invoices_InvoicesViewModel = "com.spipme.app.ui.invoices.InvoicesViewModel";

      static String com_spipme_app_ui_sync_SyncStatusViewModel = "com.spipme.app.ui.sync.SyncStatusViewModel";

      static String com_spipme_app_ui_tasks_creation_CreerTacheViewModel = "com.spipme.app.ui.tasks.creation.CreerTacheViewModel";

      static String com_spipme_app_ui_registry_creation_CreerEntiteViewModel = "com.spipme.app.ui.registry.creation.CreerEntiteViewModel";

      static String com_spipme_app_ui_registry_RegistryViewModel = "com.spipme.app.ui.registry.RegistryViewModel";

      static String com_spipme_app_ui_auth_motdepasseoublie_MotDePasseOublieViewModel = "com.spipme.app.ui.auth.motdepasseoublie.MotDePasseOublieViewModel";

      static String com_spipme_app_ui_treasury_creation_CreerTransactionViewModel = "com.spipme.app.ui.treasury.creation.CreerTransactionViewModel";

      @KeepFieldType
      TreasuryViewModel com_spipme_app_ui_treasury_TreasuryViewModel2;

      @KeepFieldType
      ComplianceViewModel com_spipme_app_ui_compliance_ComplianceViewModel2;

      @KeepFieldType
      AlertsViewModel com_spipme_app_ui_alerts_AlertsViewModel2;

      @KeepFieldType
      IntelligenceViewModel com_spipme_app_ui_intelligence_IntelligenceViewModel2;

      @KeepFieldType
      DashboardViewModel com_spipme_app_ui_dashboard_DashboardViewModel2;

      @KeepFieldType
      ImportsViewModel com_spipme_app_ui_imports_ImportsViewModel2;

      @KeepFieldType
      TasksViewModel com_spipme_app_ui_tasks_TasksViewModel2;

      @KeepFieldType
      AuditViewModel com_spipme_app_ui_audit_AuditViewModel2;

      @KeepFieldType
      CreerRessourceViewModel com_spipme_app_ui_resources_creation_CreerRessourceViewModel2;

      @KeepFieldType
      ProfileViewModel com_spipme_app_ui_profile_ProfileViewModel2;

      @KeepFieldType
      ResourcesViewModel com_spipme_app_ui_resources_ResourcesViewModel2;

      @KeepFieldType
      NotificationBadgeViewModel com_spipme_app_core_notifications_NotificationBadgeViewModel2;

      @KeepFieldType
      RegisterViewModel com_spipme_app_ui_auth_register_RegisterViewModel2;

      @KeepFieldType
      SecteurSelectorViewModel com_spipme_app_ui_components_SecteurSelectorViewModel2;

      @KeepFieldType
      AdminViewModel com_spipme_app_ui_admin_AdminViewModel2;

      @KeepFieldType
      HomeViewModel com_spipme_app_ui_home_HomeViewModel2;

      @KeepFieldType
      OfflineLoginViewModel com_spipme_app_ui_auth_offline_OfflineLoginViewModel2;

      @KeepFieldType
      AppViewModel com_spipme_app_AppViewModel2;

      @KeepFieldType
      LoginViewModel com_spipme_app_ui_auth_login_LoginViewModel2;

      @KeepFieldType
      PilotageViewModel com_spipme_app_ui_pilotage_PilotageViewModel2;

      @KeepFieldType
      InvoicesViewModel com_spipme_app_ui_invoices_InvoicesViewModel2;

      @KeepFieldType
      SyncStatusViewModel com_spipme_app_ui_sync_SyncStatusViewModel2;

      @KeepFieldType
      CreerTacheViewModel com_spipme_app_ui_tasks_creation_CreerTacheViewModel2;

      @KeepFieldType
      CreerEntiteViewModel com_spipme_app_ui_registry_creation_CreerEntiteViewModel2;

      @KeepFieldType
      RegistryViewModel com_spipme_app_ui_registry_RegistryViewModel2;

      @KeepFieldType
      MotDePasseOublieViewModel com_spipme_app_ui_auth_motdepasseoublie_MotDePasseOublieViewModel2;

      @KeepFieldType
      CreerTransactionViewModel com_spipme_app_ui_treasury_creation_CreerTransactionViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.spipme.app.ui.admin.AdminViewModel 
          return (T) new AdminViewModel(singletonCImpl.adminRepositoryImplProvider.get(), singletonCImpl.secteurRepositoryImplProvider.get());

          case 1: // com.spipme.app.ui.alerts.AlertsViewModel 
          return (T) new AlertsViewModel(singletonCImpl.alerteRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 2: // com.spipme.app.AppViewModel 
          return (T) new AppViewModel(singletonCImpl.authRepositoryImplProvider.get());

          case 3: // com.spipme.app.ui.audit.AuditViewModel 
          return (T) new AuditViewModel(singletonCImpl.auditRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 4: // com.spipme.app.ui.compliance.ComplianceViewModel 
          return (T) new ComplianceViewModel(singletonCImpl.conformiteRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 5: // com.spipme.app.ui.registry.creation.CreerEntiteViewModel 
          return (T) new CreerEntiteViewModel(singletonCImpl.entiteRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 6: // com.spipme.app.ui.resources.creation.CreerRessourceViewModel 
          return (T) new CreerRessourceViewModel(singletonCImpl.ressourceRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 7: // com.spipme.app.ui.tasks.creation.CreerTacheViewModel 
          return (T) new CreerTacheViewModel(singletonCImpl.tacheRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 8: // com.spipme.app.ui.treasury.creation.CreerTransactionViewModel 
          return (T) new CreerTransactionViewModel(singletonCImpl.transactionRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 9: // com.spipme.app.ui.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(singletonCImpl.transactionRepositoryImplProvider.get(), singletonCImpl.entiteRepositoryImplProvider.get(), singletonCImpl.ressourceRepositoryImplProvider.get(), singletonCImpl.tacheRepositoryImplProvider.get(), singletonCImpl.alerteRepositoryImplProvider.get(), singletonCImpl.suggestionRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 10: // com.spipme.app.ui.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.authRepositoryImplProvider.get());

          case 11: // com.spipme.app.ui.imports.ImportsViewModel 
          return (T) new ImportsViewModel(singletonCImpl.importRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 12: // com.spipme.app.ui.intelligence.IntelligenceViewModel 
          return (T) new IntelligenceViewModel(singletonCImpl.suggestionRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 13: // com.spipme.app.ui.invoices.InvoicesViewModel 
          return (T) new InvoicesViewModel(singletonCImpl.factureRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 14: // com.spipme.app.ui.auth.login.LoginViewModel 
          return (T) new LoginViewModel(singletonCImpl.authRepositoryImplProvider.get());

          case 15: // com.spipme.app.ui.auth.motdepasseoublie.MotDePasseOublieViewModel 
          return (T) new MotDePasseOublieViewModel(singletonCImpl.authRepositoryImplProvider.get());

          case 16: // com.spipme.app.core.notifications.NotificationBadgeViewModel 
          return (T) new NotificationBadgeViewModel(activityRetainedCImpl.notificationBadgeManagerProvider.get());

          case 17: // com.spipme.app.ui.auth.offline.OfflineLoginViewModel 
          return (T) new OfflineLoginViewModel(singletonCImpl.authRepositoryImplProvider.get());

          case 18: // com.spipme.app.ui.pilotage.PilotageViewModel 
          return (T) new PilotageViewModel(singletonCImpl.analyticsRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 19: // com.spipme.app.ui.profile.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.moiRepositoryImplProvider.get(), singletonCImpl.authRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 20: // com.spipme.app.ui.auth.register.RegisterViewModel 
          return (T) new RegisterViewModel(singletonCImpl.authRepositoryImplProvider.get());

          case 21: // com.spipme.app.ui.registry.RegistryViewModel 
          return (T) new RegistryViewModel(singletonCImpl.entiteRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 22: // com.spipme.app.ui.resources.ResourcesViewModel 
          return (T) new ResourcesViewModel(singletonCImpl.ressourceRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get(), singletonCImpl.synchronisationRepositoryProvider.get(), singletonCImpl.transactionRepositoryImplProvider.get());

          case 23: // com.spipme.app.ui.components.SecteurSelectorViewModel 
          return (T) new SecteurSelectorViewModel(singletonCImpl.secteurRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 24: // com.spipme.app.ui.sync.SyncStatusViewModel 
          return (T) new SyncStatusViewModel(singletonCImpl.synchronisationRepositoryProvider.get());

          case 25: // com.spipme.app.ui.tasks.TasksViewModel 
          return (T) new TasksViewModel(singletonCImpl.tacheRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 26: // com.spipme.app.ui.treasury.TreasuryViewModel 
          return (T) new TreasuryViewModel(singletonCImpl.transactionRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get(), singletonCImpl.synchronisationRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends SpiPmeApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private Provider<NotificationBadgeManager> notificationBadgeManagerProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
      this.notificationBadgeManagerProvider = DoubleCheck.provider(new SwitchingProvider<NotificationBadgeManager>(singletonCImpl, activityRetainedCImpl, 1));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          case 1: // com.spipme.app.core.notifications.NotificationBadgeManager 
          return (T) new NotificationBadgeManager(singletonCImpl.alerteRepositoryImplProvider.get(), singletonCImpl.sessionManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends SpiPmeApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends SpiPmeApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> fournirAppDatabaseProvider;

    private Provider<SyncScheduler> syncSchedulerProvider;

    private Provider<ConnectivityMonitor> connectivityMonitorProvider;

    private Provider<SyncActivite> syncActiviteProvider;

    private Provider<TokenManager> tokenManagerProvider;

    private Provider<SessionManager> sessionManagerProvider;

    private Provider<Json> fournirJsonProvider;

    private Provider<SynchronisationRepository> synchronisationRepositoryProvider;

    private Provider<RoomFileOperations> roomFileOperationsProvider;

    private Provider<HttpLoggingInterceptor> fournirLoggingInterceptorProvider;

    private Provider<OkHttpClient> fournirOkHttpClientBrutProvider;

    private Provider<Retrofit> fournirRetrofitBrutProvider;

    private Provider<AuthApi> fournirAuthApiBrutProvider;

    private Provider<OkHttpClient> fournirOkHttpClientAuthentifieProvider;

    private Provider<PasserelleOkHttp> passerelleOkHttpProvider;

    private Provider<RoomCorrespondances> roomCorrespondancesProvider;

    private Provider<SyncEngine> fournirMoteurProvider;

    private Provider<Retrofit> fournirRetrofitAuthentifieProvider;

    private Provider<AdminApi> fournirAdminApiProvider;

    private Provider<AdminRepositoryImpl> adminRepositoryImplProvider;

    private Provider<SecteurApi> fournirSecteurApiProvider;

    private Provider<CacheLecture> cacheLectureProvider;

    private Provider<SecteurRepositoryImpl> secteurRepositoryImplProvider;

    private Provider<AlerteApi> fournirAlerteApiProvider;

    private Provider<AlerteRepositoryImpl> alerteRepositoryImplProvider;

    private Provider<AuthApi> fournirAuthApiProvider;

    private Provider<UtilisateurLocalDao> fournirUtilisateurLocalDaoProvider;

    private Provider<AuthRepositoryImpl> authRepositoryImplProvider;

    private Provider<AuditApi> fournirAuditApiProvider;

    private Provider<AuditRepositoryImpl> auditRepositoryImplProvider;

    private Provider<ConformiteApi> fournirConformiteApiProvider;

    private Provider<ConformiteRepositoryImpl> conformiteRepositoryImplProvider;

    private Provider<EcritureHorsLigne> ecritureHorsLigneProvider;

    private Provider<EntiteApi> fournirEntiteApiProvider;

    private Provider<EntiteRepositoryImpl> entiteRepositoryImplProvider;

    private Provider<RessourceApi> fournirRessourceApiProvider;

    private Provider<RessourceRepositoryImpl> ressourceRepositoryImplProvider;

    private Provider<TacheApi> fournirTacheApiProvider;

    private Provider<TacheRepositoryImpl> tacheRepositoryImplProvider;

    private Provider<TransactionApi> fournirTransactionApiProvider;

    private Provider<TransactionRepositoryImpl> transactionRepositoryImplProvider;

    private Provider<SuggestionApi> fournirSuggestionApiProvider;

    private Provider<SuggestionRepositoryImpl> suggestionRepositoryImplProvider;

    private Provider<ImportApi> fournirImportApiProvider;

    private Provider<ImportRepositoryImpl> importRepositoryImplProvider;

    private Provider<FactureApi> fournirFactureApiProvider;

    private Provider<FactureRepositoryImpl> factureRepositoryImplProvider;

    private Provider<AnalyticsApi> fournirAnalyticsApiProvider;

    private Provider<AnalyticsRepositoryImpl> analyticsRepositoryImplProvider;

    private Provider<MoiApi> fournirMoiApiProvider;

    private Provider<MoiRepositoryImpl> moiRepositoryImplProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);
      initialize3(applicationContextModuleParam);

    }

    private OperationDao operationDao() {
      return DatabaseModule_FournirOperationDaoFactory.fournirOperationDao(fournirAppDatabaseProvider.get());
    }

    private AuthInterceptor authInterceptor() {
      return new AuthInterceptor(tokenManagerProvider.get());
    }

    private TokenAuthenticator tokenAuthenticator() {
      return new TokenAuthenticator(tokenManagerProvider.get(), fournirAuthApiBrutProvider.get());
    }

    private CorrespondanceDao correspondanceDao() {
      return DatabaseModule_FournirCorrespondanceDaoFactory.fournirCorrespondanceDao(fournirAppDatabaseProvider.get());
    }

    private CacheDao cacheDao() {
      return DatabaseModule_FournirCacheDaoFactory.fournirCacheDao(fournirAppDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.fournirAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 1));
      this.syncSchedulerProvider = DoubleCheck.provider(new SwitchingProvider<SyncScheduler>(singletonCImpl, 2));
      this.connectivityMonitorProvider = DoubleCheck.provider(new SwitchingProvider<ConnectivityMonitor>(singletonCImpl, 3));
      this.syncActiviteProvider = DoubleCheck.provider(new SwitchingProvider<SyncActivite>(singletonCImpl, 4));
      this.tokenManagerProvider = DoubleCheck.provider(new SwitchingProvider<TokenManager>(singletonCImpl, 5));
      this.sessionManagerProvider = DoubleCheck.provider(new SwitchingProvider<SessionManager>(singletonCImpl, 6));
      this.fournirJsonProvider = DoubleCheck.provider(new SwitchingProvider<Json>(singletonCImpl, 7));
      this.synchronisationRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SynchronisationRepository>(singletonCImpl, 0));
      this.roomFileOperationsProvider = DoubleCheck.provider(new SwitchingProvider<RoomFileOperations>(singletonCImpl, 9));
      this.fournirLoggingInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<HttpLoggingInterceptor>(singletonCImpl, 12));
      this.fournirOkHttpClientBrutProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 15));
      this.fournirRetrofitBrutProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 14));
      this.fournirAuthApiBrutProvider = DoubleCheck.provider(new SwitchingProvider<AuthApi>(singletonCImpl, 13));
      this.fournirOkHttpClientAuthentifieProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 11));
      this.passerelleOkHttpProvider = DoubleCheck.provider(new SwitchingProvider<PasserelleOkHttp>(singletonCImpl, 10));
      this.roomCorrespondancesProvider = DoubleCheck.provider(new SwitchingProvider<RoomCorrespondances>(singletonCImpl, 16));
      this.fournirMoteurProvider = DoubleCheck.provider(new SwitchingProvider<SyncEngine>(singletonCImpl, 8));
      this.fournirRetrofitAuthentifieProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 19));
      this.fournirAdminApiProvider = DoubleCheck.provider(new SwitchingProvider<AdminApi>(singletonCImpl, 18));
      this.adminRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AdminRepositoryImpl>(singletonCImpl, 17));
      this.fournirSecteurApiProvider = DoubleCheck.provider(new SwitchingProvider<SecteurApi>(singletonCImpl, 21));
      this.cacheLectureProvider = DoubleCheck.provider(new SwitchingProvider<CacheLecture>(singletonCImpl, 22));
      this.secteurRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SecteurRepositoryImpl>(singletonCImpl, 20));
      this.fournirAlerteApiProvider = DoubleCheck.provider(new SwitchingProvider<AlerteApi>(singletonCImpl, 24));
      this.alerteRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AlerteRepositoryImpl>(singletonCImpl, 23));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.fournirAuthApiProvider = DoubleCheck.provider(new SwitchingProvider<AuthApi>(singletonCImpl, 26));
      this.fournirUtilisateurLocalDaoProvider = DoubleCheck.provider(new SwitchingProvider<UtilisateurLocalDao>(singletonCImpl, 27));
      this.authRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepositoryImpl>(singletonCImpl, 25));
      this.fournirAuditApiProvider = DoubleCheck.provider(new SwitchingProvider<AuditApi>(singletonCImpl, 29));
      this.auditRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AuditRepositoryImpl>(singletonCImpl, 28));
      this.fournirConformiteApiProvider = DoubleCheck.provider(new SwitchingProvider<ConformiteApi>(singletonCImpl, 31));
      this.conformiteRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ConformiteRepositoryImpl>(singletonCImpl, 30));
      this.ecritureHorsLigneProvider = DoubleCheck.provider(new SwitchingProvider<EcritureHorsLigne>(singletonCImpl, 33));
      this.fournirEntiteApiProvider = DoubleCheck.provider(new SwitchingProvider<EntiteApi>(singletonCImpl, 34));
      this.entiteRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<EntiteRepositoryImpl>(singletonCImpl, 32));
      this.fournirRessourceApiProvider = DoubleCheck.provider(new SwitchingProvider<RessourceApi>(singletonCImpl, 36));
      this.ressourceRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<RessourceRepositoryImpl>(singletonCImpl, 35));
      this.fournirTacheApiProvider = DoubleCheck.provider(new SwitchingProvider<TacheApi>(singletonCImpl, 38));
      this.tacheRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<TacheRepositoryImpl>(singletonCImpl, 37));
      this.fournirTransactionApiProvider = DoubleCheck.provider(new SwitchingProvider<TransactionApi>(singletonCImpl, 40));
      this.transactionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<TransactionRepositoryImpl>(singletonCImpl, 39));
      this.fournirSuggestionApiProvider = DoubleCheck.provider(new SwitchingProvider<SuggestionApi>(singletonCImpl, 42));
      this.suggestionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SuggestionRepositoryImpl>(singletonCImpl, 41));
      this.fournirImportApiProvider = DoubleCheck.provider(new SwitchingProvider<ImportApi>(singletonCImpl, 44));
      this.importRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ImportRepositoryImpl>(singletonCImpl, 43));
      this.fournirFactureApiProvider = DoubleCheck.provider(new SwitchingProvider<FactureApi>(singletonCImpl, 46));
      this.factureRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<FactureRepositoryImpl>(singletonCImpl, 45));
      this.fournirAnalyticsApiProvider = DoubleCheck.provider(new SwitchingProvider<AnalyticsApi>(singletonCImpl, 48));
      this.analyticsRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AnalyticsRepositoryImpl>(singletonCImpl, 47));
      this.fournirMoiApiProvider = DoubleCheck.provider(new SwitchingProvider<MoiApi>(singletonCImpl, 50));
    }

    @SuppressWarnings("unchecked")
    private void initialize3(final ApplicationContextModule applicationContextModuleParam) {
      this.moiRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<MoiRepositoryImpl>(singletonCImpl, 49));
    }

    @Override
    public void injectSpiPmeApplication(SpiPmeApplication spiPmeApplication) {
      injectSpiPmeApplication2(spiPmeApplication);
    }

    @Override
    public SyncEngine moteur() {
      return fournirMoteurProvider.get();
    }

    @Override
    public SyncActivite activite() {
      return syncActiviteProvider.get();
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private SpiPmeApplication injectSpiPmeApplication2(SpiPmeApplication instance) {
      SpiPmeApplication_MembersInjector.injectSynchronisation(instance, synchronisationRepositoryProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.spipme.app.core.sync.android.SynchronisationRepository 
          return (T) new SynchronisationRepository(singletonCImpl.operationDao(), singletonCImpl.syncSchedulerProvider.get(), singletonCImpl.connectivityMonitorProvider.get(), singletonCImpl.syncActiviteProvider.get(), singletonCImpl.tokenManagerProvider.get(), singletonCImpl.sessionManagerProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 1: // com.spipme.app.data.local.AppDatabase 
          return (T) DatabaseModule_FournirAppDatabaseFactory.fournirAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.spipme.app.core.sync.android.SyncScheduler 
          return (T) new SyncScheduler(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.spipme.app.core.sync.android.ConnectivityMonitor 
          return (T) new ConnectivityMonitor(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.spipme.app.core.sync.android.SyncActivite 
          return (T) new SyncActivite();

          case 5: // com.spipme.app.core.security.TokenManager 
          return (T) new TokenManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.spipme.app.core.security.SessionManager 
          return (T) new SessionManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // kotlinx.serialization.json.Json 
          return (T) NetworkModule_FournirJsonFactory.fournirJson();

          case 8: // com.spipme.app.core.sync.SyncEngine 
          return (T) SyncModule_FournirMoteurFactory.fournirMoteur(singletonCImpl.roomFileOperationsProvider.get(), singletonCImpl.passerelleOkHttpProvider.get(), singletonCImpl.roomCorrespondancesProvider.get());

          case 9: // com.spipme.app.core.sync.android.RoomFileOperations 
          return (T) new RoomFileOperations(singletonCImpl.operationDao(), singletonCImpl.sessionManagerProvider.get());

          case 10: // com.spipme.app.core.sync.android.PasserelleOkHttp 
          return (T) new PasserelleOkHttp(singletonCImpl.fournirOkHttpClientAuthentifieProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 11: // @com.spipme.app.core.network.ClientAuthentifie okhttp3.OkHttpClient 
          return (T) NetworkModule_FournirOkHttpClientAuthentifieFactory.fournirOkHttpClientAuthentifie(singletonCImpl.fournirLoggingInterceptorProvider.get(), singletonCImpl.authInterceptor(), singletonCImpl.tokenAuthenticator());

          case 12: // okhttp3.logging.HttpLoggingInterceptor 
          return (T) NetworkModule_FournirLoggingInterceptorFactory.fournirLoggingInterceptor();

          case 13: // @com.spipme.app.core.network.ClientBrut com.spipme.app.data.remote.api.AuthApi 
          return (T) NetworkModule_FournirAuthApiBrutFactory.fournirAuthApiBrut(singletonCImpl.fournirRetrofitBrutProvider.get());

          case 14: // @com.spipme.app.core.network.ClientBrut retrofit2.Retrofit 
          return (T) NetworkModule_FournirRetrofitBrutFactory.fournirRetrofitBrut(singletonCImpl.fournirOkHttpClientBrutProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 15: // @com.spipme.app.core.network.ClientBrut okhttp3.OkHttpClient 
          return (T) NetworkModule_FournirOkHttpClientBrutFactory.fournirOkHttpClientBrut(singletonCImpl.fournirLoggingInterceptorProvider.get());

          case 16: // com.spipme.app.core.sync.android.RoomCorrespondances 
          return (T) new RoomCorrespondances(singletonCImpl.correspondanceDao());

          case 17: // com.spipme.app.data.repository.AdminRepositoryImpl 
          return (T) new AdminRepositoryImpl(singletonCImpl.fournirAdminApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 18: // com.spipme.app.data.remote.api.AdminApi 
          return (T) NetworkModule_FournirAdminApiFactory.fournirAdminApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 19: // @com.spipme.app.core.network.ClientAuthentifie retrofit2.Retrofit 
          return (T) NetworkModule_FournirRetrofitAuthentifieFactory.fournirRetrofitAuthentifie(singletonCImpl.fournirOkHttpClientAuthentifieProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 20: // com.spipme.app.data.repository.SecteurRepositoryImpl 
          return (T) new SecteurRepositoryImpl(singletonCImpl.fournirSecteurApiProvider.get(), singletonCImpl.fournirJsonProvider.get(), singletonCImpl.cacheLectureProvider.get());

          case 21: // com.spipme.app.data.remote.api.SecteurApi 
          return (T) NetworkModule_FournirSecteurApiFactory.fournirSecteurApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 22: // com.spipme.app.data.local.CacheLecture 
          return (T) new CacheLecture(singletonCImpl.cacheDao(), singletonCImpl.fournirJsonProvider.get(), singletonCImpl.sessionManagerProvider.get());

          case 23: // com.spipme.app.data.repository.AlerteRepositoryImpl 
          return (T) new AlerteRepositoryImpl(singletonCImpl.cacheLectureProvider.get(), singletonCImpl.fournirAlerteApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 24: // com.spipme.app.data.remote.api.AlerteApi 
          return (T) NetworkModule_FournirAlerteApiFactory.fournirAlerteApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 25: // com.spipme.app.data.repository.AuthRepositoryImpl 
          return (T) new AuthRepositoryImpl(singletonCImpl.fournirAuthApiProvider.get(), singletonCImpl.tokenManagerProvider.get(), singletonCImpl.sessionManagerProvider.get(), singletonCImpl.fournirUtilisateurLocalDaoProvider.get(), singletonCImpl.cacheDao(), singletonCImpl.fournirJsonProvider.get());

          case 26: // com.spipme.app.data.remote.api.AuthApi 
          return (T) NetworkModule_FournirAuthApiFactory.fournirAuthApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 27: // com.spipme.app.data.local.dao.UtilisateurLocalDao 
          return (T) DatabaseModule_FournirUtilisateurLocalDaoFactory.fournirUtilisateurLocalDao(singletonCImpl.fournirAppDatabaseProvider.get());

          case 28: // com.spipme.app.data.repository.AuditRepositoryImpl 
          return (T) new AuditRepositoryImpl(singletonCImpl.fournirAuditApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 29: // com.spipme.app.data.remote.api.AuditApi 
          return (T) NetworkModule_FournirAuditApiFactory.fournirAuditApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 30: // com.spipme.app.data.repository.ConformiteRepositoryImpl 
          return (T) new ConformiteRepositoryImpl(singletonCImpl.fournirConformiteApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 31: // com.spipme.app.data.remote.api.ConformiteApi 
          return (T) NetworkModule_FournirConformiteApiFactory.fournirConformiteApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 32: // com.spipme.app.data.repository.EntiteRepositoryImpl 
          return (T) new EntiteRepositoryImpl(singletonCImpl.ecritureHorsLigneProvider.get(), singletonCImpl.cacheLectureProvider.get(), singletonCImpl.fournirEntiteApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 33: // com.spipme.app.core.sync.android.EcritureHorsLigne 
          return (T) new EcritureHorsLigne(singletonCImpl.synchronisationRepositoryProvider.get());

          case 34: // com.spipme.app.data.remote.api.EntiteApi 
          return (T) NetworkModule_FournirEntiteApiFactory.fournirEntiteApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 35: // com.spipme.app.data.repository.RessourceRepositoryImpl 
          return (T) new RessourceRepositoryImpl(singletonCImpl.ecritureHorsLigneProvider.get(), singletonCImpl.cacheLectureProvider.get(), singletonCImpl.fournirRessourceApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 36: // com.spipme.app.data.remote.api.RessourceApi 
          return (T) NetworkModule_FournirRessourceApiFactory.fournirRessourceApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 37: // com.spipme.app.data.repository.TacheRepositoryImpl 
          return (T) new TacheRepositoryImpl(singletonCImpl.ecritureHorsLigneProvider.get(), singletonCImpl.cacheLectureProvider.get(), singletonCImpl.fournirTacheApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 38: // com.spipme.app.data.remote.api.TacheApi 
          return (T) NetworkModule_FournirTacheApiFactory.fournirTacheApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 39: // com.spipme.app.data.repository.TransactionRepositoryImpl 
          return (T) new TransactionRepositoryImpl(singletonCImpl.synchronisationRepositoryProvider.get(), singletonCImpl.ecritureHorsLigneProvider.get(), singletonCImpl.cacheLectureProvider.get(), singletonCImpl.fournirTransactionApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 40: // com.spipme.app.data.remote.api.TransactionApi 
          return (T) NetworkModule_FournirTransactionApiFactory.fournirTransactionApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 41: // com.spipme.app.data.repository.SuggestionRepositoryImpl 
          return (T) new SuggestionRepositoryImpl(singletonCImpl.fournirSuggestionApiProvider.get(), singletonCImpl.fournirJsonProvider.get(), singletonCImpl.cacheLectureProvider.get());

          case 42: // com.spipme.app.data.remote.api.SuggestionApi 
          return (T) NetworkModule_FournirSuggestionApiFactory.fournirSuggestionApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 43: // com.spipme.app.data.repository.ImportRepositoryImpl 
          return (T) new ImportRepositoryImpl(singletonCImpl.fournirImportApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 44: // com.spipme.app.data.remote.api.ImportApi 
          return (T) NetworkModule_FournirImportApiFactory.fournirImportApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 45: // com.spipme.app.data.repository.FactureRepositoryImpl 
          return (T) new FactureRepositoryImpl(singletonCImpl.fournirFactureApiProvider.get(), singletonCImpl.fournirJsonProvider.get(), singletonCImpl.cacheLectureProvider.get(), singletonCImpl.synchronisationRepositoryProvider.get());

          case 46: // com.spipme.app.data.remote.api.FactureApi 
          return (T) NetworkModule_FournirFactureApiFactory.fournirFactureApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 47: // com.spipme.app.data.repository.AnalyticsRepositoryImpl 
          return (T) new AnalyticsRepositoryImpl(singletonCImpl.fournirAnalyticsApiProvider.get(), singletonCImpl.fournirJsonProvider.get(), singletonCImpl.cacheLectureProvider.get());

          case 48: // com.spipme.app.data.remote.api.AnalyticsApi 
          return (T) NetworkModule_FournirAnalyticsApiFactory.fournirAnalyticsApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          case 49: // com.spipme.app.data.repository.MoiRepositoryImpl 
          return (T) new MoiRepositoryImpl(singletonCImpl.fournirMoiApiProvider.get(), singletonCImpl.fournirJsonProvider.get());

          case 50: // com.spipme.app.data.remote.api.MoiApi 
          return (T) NetworkModule_FournirMoiApiFactory.fournirMoiApi(singletonCImpl.fournirRetrofitAuthentifieProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
