/*
 * Copyright 2013-2014 the original author or authors.
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

package org.springframework.guice.module;

import javax.inject.Inject;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Dave Syer
 *
 */
public class SpringModuleMetadataTests {

	@Test
	public void twoConfigClasses() throws Exception {
		Injector injector = createInjector(TestConfig.class, OtherConfig.class);
		assertThat(injector.getInstance(Service.class)).isNotNull();
	}

	@Test
	public void twoServices() throws Exception {
		Injector injector = createInjector(TestConfig.class, MoreConfig.class);
		assertThatExceptionOfType(ProvisionException.class)
				.isThrownBy(() -> assertThat(injector.getInstance(Service.class)).isNotNull());
	}

	@Test
	public void twoServicesOnePrimary() throws Exception {
		Injector injector = createInjector(TestConfig.class, PrimaryConfig.class);
		assertThat(injector.getInstance(Service.class)).isNotNull();
	}

	@Test
	public void twoServicesByName() throws Exception {
		Injector injector = createInjector(TestConfig.class, MoreConfig.class);
		assertThat(injector.getInstance(Key.get(Service.class, Names.named("service")))).isNotNull();
	}

	@Test
	public void includes() throws Exception {
		Injector injector = createInjector(TestConfig.class, MetadataIncludesConfig.class);
		assertThatExceptionOfType(ConfigurationException.class)
				.isThrownBy(() -> assertThat(injector.getBinding(Service.class)).isNull());
	}

	@Test
	public void excludes() throws Exception {
		Injector injector = createInjector(TestConfig.class, MetadataExcludesConfig.class);
		assertThatExceptionOfType(ConfigurationException.class)
				.isThrownBy(() -> assertThat(injector.getBinding(Service.class)).isNull());
	}

	private Injector createInjector(Class<?>... config) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(config);
		context.refresh();
		Injector injector = Guice.createInjector(new SpringModule(context));
		return injector;
	}

	interface Service {

	}

	protected static class MyService implements Service {

	}

	public static class Foo {

		@Inject
		public Foo(Service service) {
		}

	}

	@Configuration
	protected static class MetadataExcludesConfig {

		@Bean
		public GuiceModuleMetadata guiceModuleMetadata() {
			GuiceModuleMetadata metadata = new GuiceModuleMetadata();
			metadata.exclude(new AssignableTypeFilter(Service.class));
			return metadata;
		}

	}

	@Configuration
	protected static class MetadataIncludesConfig {

		@Bean
		public GuiceModuleMetadata guiceModuleMetadata() {
			GuiceModuleMetadata metadata = new GuiceModuleMetadata();
			metadata.include(new AnnotationTypeFilter(Cacheable.class));
			return metadata;
		}

	}

	@Configuration
	public static class TestConfig {

		@Bean
		public Service service() {
			return new MyService();
		}

	}

	@Configuration
	public static class PrimaryConfig {

		@Bean
		@Primary
		public Service primary() {
			return new MyService();
		}

	}

	@Configuration
	public static class MoreConfig {

		@Bean
		public Service more() {
			return new MyService();
		}

	}

	@Configuration
	public static class OtherConfig {

	}

}
