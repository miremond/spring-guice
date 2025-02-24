/*
 * Copyright 2018-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.guice;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Module;
import com.google.inject.multibindings.OptionalBinder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.guice.annotation.EnableGuiceModules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class BindingDeduplicationTests {

	@AfterAll
	public static void cleanUp() {
		System.clearProperty("spring.guice.dedup");
	}

	@Test
	public void verifyNoDuplicateBindingErrorWhenDedupeEnabled() {
		System.setProperty("spring.guice.dedup", "true");
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				BindingDeduplicationTestsConfig.class);
		SomeDependency someDependency = context.getBean(SomeDependency.class);
		assertThat(someDependency).isNotNull();
		SomeOptionalDependency someOptionalDependency = context.getBean(SomeOptionalDependency.class);
		assertThat(someOptionalDependency).isNotNull();
		context.close();
	}

	@Test
	public void verifyDuplicateBindingErrorWhenDedupeNotEnabled() {
		System.setProperty("spring.guice.dedup", "false");
		assertThatExceptionOfType(CreationException.class).isThrownBy(() -> {
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
					BindingDeduplicationTestsConfig.class);
			context.close();
		});
	}

	public static class SomeDependency {

	}

	public static class SomeOptionalDependency {

	}

	@EnableGuiceModules
	@Configuration
	static class BindingDeduplicationTestsConfig {

		@Bean
		SomeDependency someBean() {
			return new SomeDependency();
		}

		@Bean
		SomeOptionalDependency someOptionalBean() {
			return new SomeOptionalDependency();
		}

		@Bean
		static Module module() {
			return new AbstractModule() {
				@Override
				protected void configure() {
					bind(SomeDependency.class).asEagerSingleton();
					OptionalBinder.newOptionalBinder(binder(), SomeOptionalDependency.class).setDefault()
							.to(SomeOptionalDependency.class);
				}
			};
		}

	}

}
