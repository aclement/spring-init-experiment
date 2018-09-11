/*
 * Copyright 2018 the original author or authors.
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

package boot.generated;

import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import slim.SlimConfiguration;

/**
 * @author Dave Syer
 *
 */
@Configuration
@EnableConfigurationProperties
@SlimConfiguration(module=AutoConfigurationModule.class)
public class AutoConfiguration {

    public static ApplicationContextInitializer<GenericApplicationContext> initializer() {
        return new Initializer();
    }

    private static class Initializer
            implements ApplicationContextInitializer<GenericApplicationContext> {

        @Override
        public void initialize(GenericApplicationContext context) {
            // TODO: how to get from @EnableConfigurationProperties to this?
            new ConfigurationPropertiesBindingPostProcessorRegistrar()
                    .registerBeanDefinitions(null, context);
        }

    }


}
