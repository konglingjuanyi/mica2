/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.mica.security;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import net.sf.ehcache.CacheManager;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.PermissionResolverAware;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.RolePermissionResolverAware;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.ExecutorServiceSessionValidationScheduler;
import org.apache.shiro.session.mgt.SessionValidationScheduler;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.config.WebIniSecurityManagerFactory;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.obiba.shiro.SessionStorageEvaluator;
import org.obiba.shiro.realm.ObibaRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import static org.obiba.mica.security.AuthoritiesConstants.ADMIN;

//@Component
//@DependsOn("cacheConfiguration")
public class SecurityManagerFactory implements FactoryBean<SecurityManager> {

  public static final String INI_REALM = "mica-ini-realm";

  private static final long SESSION_VALIDATION_INTERVAL = 3600000l; // 1 hour

  private static final Logger log = LoggerFactory.getLogger(SecurityManagerFactory.class);

  @Inject
  private Environment env;

  @Inject
  private Set<Realm> realms;

//  @Inject
//  private Set<SessionListener> sessionListeners;

//  @Inject
//  private Set<AuthenticationListener> authenticationListeners;

  @Inject
  private RolePermissionResolver rolePermissionResolver;

  @Inject
  private PermissionResolver permissionResolver;

  @Inject
  private CacheManager cacheManager;

  private SecurityManager securityManager;

  @Override
  public SecurityManager getObject() throws Exception {
    if(securityManager == null) {
      securityManager = doCreateSecurityManager();
      SecurityUtils.setSecurityManager(securityManager);
    }
    return securityManager;
  }

  @Override
  public Class<?> getObjectType() {
    return DefaultSecurityManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @PreDestroy
  public void destroySecurityManager() {
    log.debug("Shutdown SecurityManager");
    // Destroy the security manager.
    SecurityUtils.setSecurityManager(null);
    LifecycleUtils.destroy(securityManager);
    securityManager = null;
  }

  private SecurityManager doCreateSecurityManager() {
    return new CustomIniSecurityManagerFactory(getShiroIniPath()).createInstance();
  }

  private String getShiroIniPath() {
    try {
      return new DefaultResourceLoader().getResource("classpath:/shiro.ini").getFile().getAbsolutePath();
    } catch(IOException e) {
      throw new RuntimeException("Cannot load shiro.ini", e);
    }
  }

  private class CustomIniSecurityManagerFactory extends WebIniSecurityManagerFactory {

    private CustomIniSecurityManagerFactory(String resourcePath) {
      super(Ini.fromResourcePath(resourcePath));
    }

    @Override
    @SuppressWarnings("ChainOfInstanceofChecks")
    protected SecurityManager createDefaultInstance() {
      DefaultWebSecurityManager dsm = (DefaultWebSecurityManager) super.createDefaultInstance();

      initializeCacheManager(dsm);
      initializeSessionManager(dsm);
      initializeSubjectDAO(dsm);
      initializeAuthorizer(dsm);
      initializeAuthenticator(dsm);

      return dsm;
    }

    private void initializeCacheManager(DefaultSecurityManager dsm) {
      if(dsm.getCacheManager() == null) {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager(cacheManager);
        dsm.setCacheManager(ehCacheManager);
      }
    }

    private void initializeSessionManager(DefaultSecurityManager dsm) {
      if(dsm.getSessionManager() instanceof DefaultSessionManager) {
        DefaultSessionManager sessionManager = (DefaultSessionManager) dsm.getSessionManager();
//        sessionManager.setSessionListeners(sessionListeners);
        sessionManager.setSessionDAO(new EnterpriseCacheSessionDAO());
        SessionValidationScheduler sessionValidationScheduler = new ExecutorServiceSessionValidationScheduler();
        sessionValidationScheduler.enableSessionValidation();
        sessionManager.setSessionValidationScheduler(sessionValidationScheduler);
        sessionManager.setSessionValidationInterval(SESSION_VALIDATION_INTERVAL);
      }
    }

    private void initializeSubjectDAO(DefaultSecurityManager dsm) {
      if(dsm.getSubjectDAO() instanceof DefaultSubjectDAO) {
        ((DefaultSubjectDAO) dsm.getSubjectDAO()).setSessionStorageEvaluator(new SessionStorageEvaluator());
      }
    }

    private void initializeAuthorizer(DefaultSecurityManager dsm) {
      if(dsm.getAuthorizer() instanceof ModularRealmAuthorizer) {
        ((RolePermissionResolverAware) dsm.getAuthorizer()).setRolePermissionResolver(rolePermissionResolver);
        ((PermissionResolverAware) dsm.getAuthorizer()).setPermissionResolver(permissionResolver);
      }
    }

    private void initializeAuthenticator(DefaultSecurityManager dsm) {
      //((AbstractAuthenticator) dsm.getAuthenticator()).setAuthenticationListeners(authenticationListeners);

      if(dsm.getAuthenticator() instanceof ModularRealmAuthenticator) {
        ((ModularRealmAuthenticator) dsm.getAuthenticator()).setAuthenticationStrategy(new FirstSuccessfulStrategy());
      }
    }

    @Override
    protected void applyRealmsToSecurityManager(Collection<Realm> shiroRealms, @SuppressWarnings(
        "ParameterHidesMemberVariable") SecurityManager securityManager) {
      ImmutableList.Builder<Realm> builder = ImmutableList.<Realm>builder().addAll(realms).addAll(shiroRealms);
      RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "agate.");
      String obibaRealmUrl = propertyResolver.getProperty("url");
      String serviceName = propertyResolver.getProperty("application.name");
      String serviceKey = propertyResolver.getProperty("application.key");

      if(!Strings.isNullOrEmpty(obibaRealmUrl)) {
        ObibaRealm oRealm = new ObibaRealm();
        oRealm.setRolePermissionResolver(rolePermissionResolver);
        oRealm.setBaseUrl(obibaRealmUrl);
        oRealm.setServiceName(serviceName);
        oRealm.setServiceKey(serviceKey);
        // Note: authentication caching is not enabled because it makes the SSO fail

        builder.add(oRealm);
      }
      super.applyRealmsToSecurityManager(builder.build(), securityManager);
    }

    @Override
    protected Realm createRealm(Ini ini) {
      // Set the resolvers first, because IniRealm is initialized before the resolvers
      // are applied by the ModularRealmAuthorizer
      IniRealm realm = new IniRealm();
      realm.setName(INI_REALM);
      realm.setRolePermissionResolver(rolePermissionResolver);
      realm.setPermissionResolver(permissionResolver);
      realm.setResourcePath(getShiroIniPath());
      realm.setCredentialsMatcher(new PasswordMatcher());
      realm.setIni(ini);
      configureUrl(realm.getIni().getSection("urls") == null
          ? realm.getIni().addSection("urls")
          : realm.getIni().getSection("urls"));
      return realm;
    }

    @SuppressWarnings("OverlyLongMethod")
    private void configureUrl(@SuppressWarnings("TypeMayBeWeakened") Ini.Section urls) {
      urls.put("/ws/logs/**", ADMIN);
      urls.put("/websocket/tracker", ADMIN);
      urls.put("/jvm*", ADMIN);
      urls.put("/jvm/**", ADMIN);
      urls.put("/health*", ADMIN);
      urls.put("/health/**", ADMIN);
      urls.put("/trace*", ADMIN);
      urls.put("/trace/**", ADMIN);
      urls.put("/dump*", ADMIN);
      urls.put("/dump/**", ADMIN);
      urls.put("/shutdown*", ADMIN);
      urls.put("/shutdown/**", ADMIN);
      urls.put("/beans*", ADMIN);
      urls.put("/beans/**", ADMIN);
      urls.put("/info*", ADMIN);
      urls.put("/info/**", ADMIN);
      urls.put("/autoconfig*", ADMIN);
      urls.put("/autoconfig/**", ADMIN);
      urls.put("/env*", ADMIN);
      urls.put("/env/**", ADMIN);
      urls.put("/trace*", ADMIN);
      urls.put("/trace/**", ADMIN);
    }
  }

}
