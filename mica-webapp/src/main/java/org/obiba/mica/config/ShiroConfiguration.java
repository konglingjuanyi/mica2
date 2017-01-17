/*
 *
 *  * Copyright (c) 2017 OBiBa. All rights reserved.
 *  *
 *  * This program and the accompanying materials
 *  * are made available under the terms of the GNU Public License v3.0.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.obiba.mica.config;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.obiba.mica.security.SecurityManagerFactory;
import org.obiba.mica.security.realm.MicaAuthorizingRealm;
import org.obiba.shiro.realm.ObibaRealm;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

import static java.util.Arrays.asList;

@Configuration
public class ShiroConfiguration {

  @Bean
  @DependsOn("lifecycleBeanPostProcessor")
  public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
    DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
    proxyCreator.setProxyTargetClass(true);
    return proxyCreator;
  }

  @Bean
  public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
    return new AuthorizationAttributeSourceAdvisor();
  }

  @Autowired
  @Bean
  @DependsOn({"realmMica", "realmObiba", "rolePermissionResolver", "cacheManager"})
  public ShiroFilterFactoryBean shiroFilter(SecurityManagerFactory securityManagerFactory) {
    return new RetardedShiroFilterFactoryBean(securityManagerFactory);
  }

  class RetardedShiroFilterFactoryBean extends ShiroFilterFactoryBean {
    private SecurityManagerFactory securityManagerFactory;

    public RetardedShiroFilterFactoryBean(SecurityManagerFactory securityManagerFactory) {
      this.securityManagerFactory = securityManagerFactory;
    }

    @Override
    protected AbstractShiroFilter createInstance() throws Exception {
      this.setSecurityManager(securityManagerFactory.getObject());
      return super.createInstance();
    }
  }

//  @Bean(name = "securityManager")
//  public DefaultWebSecurityManager securityManager() {
//    final DefaultWebSecurityManager securityManager
//      = new DefaultWebSecurityManager();
//    securityManager.setRealms(asList(realmMica(), realmObiba()));
//    securityManager.setSessionManager(sessionManager());
//    return securityManager;
//  }

  @Bean
  @DependsOn("realmMica")
  public SecurityManagerFactory securityManagerFactory() {
    return new SecurityManagerFactory();
  }

  @Bean
  public DefaultWebSessionManager sessionManager() {
    final DefaultWebSessionManager sessionManager
      = new DefaultWebSessionManager();
    sessionManager.setGlobalSessionTimeout(43200000); // 12 hours
    return sessionManager;
  }

  @Bean
  @DependsOn("cacheManager")
  public RolePermissionResolver rolePermissionResolver(MicaAuthorizingRealm micaAuthorizingRealm){
    return micaAuthorizingRealm;
  }

//  @Bean
//  public SessionDAO sessionDao() {
//    return new HazelcastSessionDao();
//  }

  @Bean
  @DependsOn("lifecycleBeanPostProcessor")
  public MicaAuthorizingRealm realmMica() {
    final MicaAuthorizingRealm realm = new MicaAuthorizingRealm();
    realm.setCredentialsMatcher(credentialsMatcher());
    return realm;
  }

  @Bean
  @DependsOn("lifecycleBeanPostProcessor")
  public ObibaRealm realmObiba() {
    final ObibaRealm realm = new ObibaRealm();
    realm.setCredentialsMatcher(credentialsMatcher());
    return realm;
  }

  @Bean(name = "credentialsMatcher")
  public PasswordMatcher credentialsMatcher() {
    final PasswordMatcher credentialsMatcher = new PasswordMatcher();
    credentialsMatcher.setPasswordService(passwordService());
    return credentialsMatcher;
  }

  @Bean(name = "passwordService")
  public DefaultPasswordService passwordService() {
    return new DefaultPasswordService();
  }

  @Bean
  public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
    return new LifecycleBeanPostProcessor();
  }

  @Bean
  public CacheManager cacheManager(){
    return new MemoryConstrainedCacheManager();
  }
}
