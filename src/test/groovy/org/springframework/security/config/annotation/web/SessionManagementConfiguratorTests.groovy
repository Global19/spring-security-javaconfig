/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.security.config.annotation.web

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.BaseSpringSpec
import org.springframework.security.config.annotation.authentication.AuthenticationManagerBuilder
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.security.web.session.ConcurrentSessionFilter
import org.springframework.security.web.session.SessionManagementFilter

/**
 *
 * @author Rob Winch
 */
class SessionManagementConfiguratorTests extends BaseSpringSpec {

    def "sessionManagement does not override ExceptionHandlingConfiurator.requestCache"() {
        setup:
            SessionManagementDoesNotOverrideExplicitRequestCacheConfig.REQUEST_CACHE = Mock(RequestCache)
        when:
            loadConfig(SessionManagementDoesNotOverrideExplicitRequestCacheConfig)
        then:
            findFilter(ExceptionTranslationFilter).requestCache == SessionManagementDoesNotOverrideExplicitRequestCacheConfig.REQUEST_CACHE
    }

    @EnableWebSecurity
    @Configuration
    static class SessionManagementDoesNotOverrideExplicitRequestCacheConfig extends WebSecurityConfigurerAdapter {
        static RequestCache REQUEST_CACHE

        @Override
        protected void configure(HttpConfiguration http) throws Exception {
            http
                .exceptionHandling()
                    .requestCache(REQUEST_CACHE)
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.stateless)
        }

    }

    def "sessionManagement LifecycleManager"() {
        setup:
            LifecycleManager lifecycleManager = Mock()
            HttpConfiguration http = new HttpConfiguration(lifecycleManager, authenticationBldr)
        when:
            http
                .sessionManagement()
                    .maximumSessions(1)
                    .and()
                .build()

        then: "SessionManagementFilter is registered with LifecycleManager"
            1 * lifecycleManager.registerLifecycle(_ as SessionManagementFilter) >> {SessionManagementFilter o -> o}
        and: "ConcurrentSessionFilter is registered with LifecycleManager"
            1 * lifecycleManager.registerLifecycle(_ as ConcurrentSessionFilter) >> {ConcurrentSessionFilter o -> o}
    }
}
